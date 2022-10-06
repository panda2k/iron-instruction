import { NextPage } from "next";
import { useState } from "react";
import { Athlete, Day, Exercise, PercentageOptions, Program, UserType } from "../utils/api.types";
import Accordion from "./Accordion";
import CreateExercise from "./CreateExercise";
import Api from '../utils/api'
import Modal from "./Modal";
import { createFFmpeg, fetchFile } from '@ffmpeg/ffmpeg'
import { NeedLogin } from "../utils/api.errors";
import { useUserContext } from "../context/UserContext";

type Props = {
    userType: UserType,
    setProgram: (program: Program) => void,
    day: Day,
    weekId: string,
    programId: string
}

const defaultExerciseRepEditingData = {
    exerciseId: "",
    setId: "",
    totalReps: 0,
    repsDone: 0
}

const ExerciseList: NextPage<Props> = (props: Props) => {
    const [newExerciseModalOpen, setNewExerciseModalOpen] = useState<boolean>(false)
    const [editing, setEditing] = useState<boolean>(false)
    const [activeExerciseEdit, setActiveExerciseEdit] = useState<Exercise | null>(null)
    const [fileUploadModalOpen, setFileUploadModalOpen] = useState<boolean>(false)
    const [video, setVideo] = useState<File | null>(null)
    const [uploading, setUploading] = useState<boolean>(false)
    const [viewVideoModalOpen, setViewVideoModalOpen] = useState<boolean>(false)
    const [viewVideoLink, setViewVideoLink] = useState<string>("")
    const [uploadVideoModalData, setUploadVideoModalData] = useState<{ exerciseId: string, setId: string }>({ exerciseId: "", setId: "" })
    const [exerciseRepEditing, setExerciseRepEditing] = useState<boolean>(false)
    const [exerciseRepEditingData, setExerciseRepEditingData] = useState<{ exerciseId: string, setId: string, totalReps: number, repsDone: number }>(defaultExerciseRepEditingData)
    const [videoUploadError, setVideoUploadError] = useState<string>("")
    const { user } = useUserContext()

    const ffmpeg = createFFmpeg({
        corePath: 'https://unpkg.com/@ffmpeg/core@0.11.0/dist/ffmpeg-core.js',
    })

    const deleteExercise = async (exerciseId: string) => {
        const program: Program = await Api.deleteExercise(props.programId, props.weekId, props.day.id, exerciseId)
        props.setProgram(program)
    }

    const toggleEditing = () => {
        setEditing(!editing)
    }

    const finishSet = async (exerciseId: string, setId: string, repsDone: number) => {
        const program: Program = await Api.finishSet(
            props.programId,
            props.weekId,
            props.day.id,
            exerciseId,
            setId,
            repsDone
        )

        props.setProgram(program)
    }

    const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setVideo((e.currentTarget.files as FileList)[0])
    }

    const getVideoUpload = async (exerciseId: string, setId: string) => {
        setUploadVideoModalData({ exerciseId: exerciseId, setId: setId })

        setFileUploadModalOpen(true)
    }

    const uploadVideo = async () => {
        if (!video?.type.includes("video")) {
            setVideoUploadError("Please make sure you are uploading a video")
            return
        }

        setUploading(true)
        let uploadUrl: string
        try {
            uploadUrl = await Api.getVideoUploadLink(
                props.programId,
                props.weekId,
                props.day.id,
                uploadVideoModalData.exerciseId,
                uploadVideoModalData.setId
            )
        } catch (error) {
            if (error instanceof NeedLogin) {
                window.location.href = "/login"
            } else {
                setVideoUploadError("Unexpected error. Please try again later")
            }

            setUploading(false)
            setVideo(null)
            return
        }

        try {
            await ffmpeg.load()

            ffmpeg.FS('writeFile', 'input.mp4', await fetchFile(video as File))
            await ffmpeg.run('-i', 'input.mp4', '-codec', 'copy', 'output.mp4')
        } catch (error) {
            setVideoUploadError("Error when encoding video. Try again later or use a different file")

            setUploading(false)
            setVideo(null)
            ffmpeg.exit()
            return
        }

        const convertedVideo: Blob = new Blob(
            [ffmpeg.FS('readFile', 'output.mp4').buffer],
            { type: 'video/mp4' }
        )
        try {
            await Api.uploadVideo(uploadUrl, convertedVideo)
        } catch (error) {
            setVideoUploadError("Error when uploading video. Please try again later")

            setUploading(false)
            setVideo(null)
            ffmpeg.exit()
            return
        }

        setFileUploadModalOpen(false)
        setUploading(false)
        setVideo(null)
        ffmpeg.exit()
        setVideoUploadError("")
    }

    const generateUploadModalContent = (): React.ReactElement => {
        return (
            <div className="flex flex-col">
                <div className="flex flex-col items-center">
                    <h1 className="text-2xl mb-4 underline">Video Upload</h1>
                    {videoUploadError && <p className="bold underline text-lg text-red-500">{videoUploadError}</p>}
                    <form>
                        <label>Video: </label>
                        <input type="file" accept="video/*" onChange={handleFileChange} />
                    </form>
                    {video && <video className="max-h-72" controls={true} src={URL.createObjectURL(video)}></video>}
                </div>
                <div className="flex flex-row justify-end">
                    <input disabled={uploading} onClick={() => setFileUploadModalOpen(false)} type="button" value="Cancel" className="disabled:opacity-80 disabled:cursor-progress purple-bg mt-6 cursor-pointer py-1 px-6 rounded-md text-white mr-3" />
                    <input disabled={uploading} onClick={uploadVideo} type="button" value={uploading ? "Uploading..." : "Upload"} className="disabled:opacity-80 disabled:cursor-progress purple-bg mt-6 cursor-pointer py-1 px-6 rounded-md text-white" />
                </div>
            </div>
        )
    }

    const getVideoViewingLink = async (exerciseId: string, setId: string) => {
        const link = await Api.getVideoViewLink(props.programId, props.weekId, props.day.id, exerciseId, setId)
        setViewVideoLink(link)
    }

    const generateVideoModalContent = (): React.ReactElement => {
        return (
            <div className="flex justify-center">
                <div className="max-h-[80vh]">
                    <video className="w-full h-auto max-h-full" src={viewVideoLink} crossOrigin="anonymous" typeof="video/mp4" controls={true}>
                    </video>
                </div>
            </div>
        )
    }

    const handleRepsDoneChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setExerciseRepEditingData({ ...exerciseRepEditingData, repsDone: parseInt(e.target.value) })
    }

    const generateEditRepContent = (): React.ReactElement => {
        return (
            <div className="flex flex-col items-center">
                <h1 className="text-2xl underline">Missed Set</h1>
                <form className="flex mt-2">
                    <label>Reps Completed: </label>
                    <input className="w-7 px-1 ml-1 mr-1 rounded-sm border border-black" type="number" value={exerciseRepEditingData.repsDone} onChange={handleRepsDoneChange} />
                    <p>/ {exerciseRepEditingData.totalReps}</p>
                </form>
                <div className="flex justify-end w-full mt-4">
                    <input onClick={() => setExerciseRepEditing(false)} type="button" value="Cancel" className="purple-bg rounded-md text-white hover:cursor-pointer py-1 px-6" />
                    <input onClick={() => {
                        finishSet(
                            exerciseRepEditingData.exerciseId,
                            exerciseRepEditingData.setId,
                            exerciseRepEditingData.repsDone
                        ); setExerciseRepEditing(false)
                    }} type="button" value="Save" className="ml-4 purple-bg rounded-md text-white hover:cursor-pointer py-1 px-6" />
                </div>
            </div>
        )
    }

    const calculateWeight = (percentageRef: PercentageOptions, percentage: number): number => {
        const athlete = user as Athlete
        percentage = percentage / 100
        if (percentageRef.valueOf() == PercentageOptions.Bench.valueOf()) {
            return 2.5 * Math.ceil((percentage * athlete.benchMax) / 2.5)
        } else if (percentageRef.valueOf() == PercentageOptions.Squat.valueOf()) {
            return 2.5 * Math.ceil((percentage * athlete.squatMax) / 2.5)
        } else if (percentageRef.valueOf() == PercentageOptions.Deadlift.valueOf()) {
            return 2.5 * Math.ceil((percentage * athlete.deadliftMax) / 2.5)
        } else {
            throw new Error("Unexpected percentage reference")
        }
    }

    const generateExerciseContent = (exercises: Exercise[]): { conditionalClick?: Function, headerExtras?: React.ReactElement, heading: string, body: React.ReactElement }[] => {
        return exercises.map(exercise => {
            return {
                heading: exercise.name,
                body: (
                    <div>
                        {
                            exercise.sets.map(set => {
                                return (
                                    <div className="flex flex-col" key={set.id}>
                                        <div className="flex flex-row justify-between">
                                            <h1>
                                                {set.reps} reps
                                                {set.rpe == -1 ?
                                                    ` at ${set.percentage}% of ${set.percentageReference.toLowerCase()} max` +
                                                    (props.userType == UserType.ATHLETE ?
                                                        ` (${calculateWeight(set.percentageReference, set.percentage)} kg)`
                                                        :
                                                        ""
                                                    )
                                                    :
                                                    ` at ${set.weight}kg with RPE ${set.rpe}`
                                                }
                                            </h1>
                                            {
                                                props.userType == UserType.ATHLETE &&
                                                <div className="flex flex-row">
                                                    <svg onClick={() => finishSet(exercise.id, set.id, set.reps)} xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className="w-6 h-6 mr-1.5 hover:cursor-pointer">
                                                        <path strokeLinecap="round" strokeLinejoin="round" d="M4.5 12.75l6 6 9-13.5" />
                                                    </svg>
                                                    <svg onClick={() => {
                                                        setExerciseRepEditing(true)
                                                        setExerciseRepEditingData({
                                                            repsDone: 0,
                                                            totalReps: set.reps,
                                                            exerciseId: exercise.id,
                                                            setId: set.id
                                                        })
                                                    }} xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className="w-6 h-6 hover:cursor-pointer">
                                                        <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
                                                    </svg>
                                                </div>
                                            }
                                        </div>
                                        {set.videoRequested &&
                                            <div className="flex flex-row justify-between">
                                                <h2 className="ml-0.5">- Video Requested</h2>
                                                {
                                                    props.userType == UserType.ATHLETE &&
                                                    <svg onClick={() => getVideoUpload(exercise.id, set.id)} xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className="w-6 h-6 hover:cursor-pointer">
                                                        <path strokeLinecap="round" strokeLinejoin="round" d="M3 16.5v2.25A2.25 2.25 0 005.25 21h13.5A2.25 2.25 0 0021 18.75V16.5m-13.5-9L12 3m0 0l4.5 4.5M12 3v13.5" />
                                                    </svg>
                                                }
                                            </div>
                                        }
                                        {set.completedReps != -1 &&
                                            <h2 className="ml-0.5">- Completed {`${set.completedReps} / ${set.reps} reps`}</h2>
                                        }
                                        {set.videoRef &&
                                            <button onClick={() => { getVideoViewingLink(exercise.id, set.id); setViewVideoModalOpen(true) }} className="ml-0.5 flex justify-start">
                                                - <h1 className="underline hover:cursor-pointer ml-1">View Video</h1>
                                            </button>
                                        }
                                    </div>
                                )
                            })
                        }
                        <Modal open={exerciseRepEditing} setOpen={setExerciseRepEditing} >
                            {generateEditRepContent()}
                        </Modal>
                        <Modal open={viewVideoModalOpen} setOpen={setViewVideoModalOpen} >
                            {generateVideoModalContent()}
                        </Modal>
                        <Modal open={fileUploadModalOpen} setOpen={setFileUploadModalOpen} >
                            {generateUploadModalContent()}
                        </Modal>
                    </div >
                ),
                ...(editing) && {
                    headerExtras: (
                        <svg onClick={() => deleteExercise(exercise.id)} xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className="w-6 h-6 mr-3 mt-2 -mb-0.5 hover:cursor-pointer">
                            <path strokeLinecap="round" strokeLinejoin="round" d="M19.5 12h-15" />
                        </svg>
                    ),
                    conditionalClick: () => {
                        setActiveExerciseEdit(exercise)
                        setNewExerciseModalOpen(true)
                    }
                }
            }
        })
    }

    return (
        <div>
            <div className={"flex flex-row items-end border-b border-b-black pb-1" + (props.userType == UserType.COACH ? " justify-between" : " justify-center")}>
                {props.userType == UserType.COACH &&
                    <h2 onClick={toggleEditing} className="h-fit hover:cursor-pointer">
                        {editing ? "Cancel" : "Edit"}
                    </h2>
                }
                <h2 className="text-lg w-fit font-medium">Exercises</h2>
                {props.userType == UserType.COACH &&
                    <svg onClick={() => { setActiveExerciseEdit(null); setNewExerciseModalOpen(true) }} xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className="w-5 h-5 mb-0.5 hover:cursor-pointer">
                        <path strokeLinecap="round" strokeLinejoin="round" d="M12 4.5v15m7.5-7.5h-15" />
                    </svg>
                }
            </div>
            <div>
                {props.day.exercises.length == 0 && <h1>No exercises</h1>}
                <Accordion items={generateExerciseContent(props.day.exercises)} animationTime={200} loaded={true} />
            </div>
            <CreateExercise setProgram={props.setProgram} open={newExerciseModalOpen} setOpen={setNewExerciseModalOpen} day={props.day} exercise={activeExerciseEdit} weekId={props.weekId} programId={props.programId} />
        </div >
    )
}

export default ExerciseList

