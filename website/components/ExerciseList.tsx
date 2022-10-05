import { NextPage } from "next";
import { useState } from "react";
import { Day, Exercise, Program, UserType } from "../utils/api.types";
import Accordion from "./Accordion";
import CreateExercise from "./CreateExercise";
import Api from '../utils/api'
import Link from "next/link";

type Props = {
    userType: UserType,
    setProgram: (program: Program) => void,
    day: Day,
    weekId: string,
    programId: string
}

const ExerciseList: NextPage<Props> = (props: Props) => {
    const [newExerciseModalOpen, setNewExerciseModalOpen] = useState<boolean>(false)
    const [editing, setEditing] = useState<boolean>(false)
    const [activeExerciseEdit, setActiveExerciseEdit] = useState<Exercise | null>(null)

    const deleteExercise = async (exerciseId: string) => {
        const program: Program = await Api.deleteExercise(props.programId, props.weekId, props.day.id, exerciseId)
        props.setProgram(program)
    }

    const toggleEditing = () => {
        setEditing(!editing)
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
                                        <h1>{set.reps} reps {set.rpe == -1 ? `at ${set.percentage}%` : `at ${set.weight}kg with RPE ${set.rpe}`}</h1>
                                        {set.videoRequested &&
                                            <h2 className="ml-0.5">- Video Requested</h2>
                                        }
                                        {set.completedReps != -1 &&
                                            <h2 className="ml-0.5">- Completed {`${set.completedReps} / ${set.reps} reps`}</h2>
                                        }
                                        {set.videoRef &&
                                            <a className="underline ml-0.5">
                                                <Link href={set.videoRef}>Video</Link>
                                            </a>
                                        }
                                    </div>
                                )
                            })
                        }
                    </div>
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
            <div className="flex flex-row justify-between items-end border-b border-b-black pb-1">
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

