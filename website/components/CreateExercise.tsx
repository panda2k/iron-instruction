import { NextPage } from "next";
import { useEffect, useState } from "react";
import { Day, Exercise, PercentageOptions, Program, Set } from "../utils/api.types";
import Modal from "./Modal";
import Api from '../utils/api'
import { ApiError, NeedLogin } from "../utils/api.errors";

type Props = {
    day: Day
    exercise: Exercise | null
    weekId: string
    programId: string,
    open: boolean,
    setOpen: (open: boolean) => void,
    setProgram: (program: Program) => void
}

const defaultExercise: Exercise = {
    name: "",
    id: "",
    sets: [],
    videoRef: ""
}

type SetForm = {
    type: "RPE" | "PERCENTAGE"
    reps: number,
    rpe: number,
    percentage: number,
    weight: number,
    percentageReference: PercentageOptions,
    videoRequested: boolean,
    editingIndex: number
}

const defaultSetForm: SetForm = {
    type: "PERCENTAGE",
    reps: 0,
    rpe: 0,
    percentage: 0,
    weight: 0,
    percentageReference: PercentageOptions.Squat,
    videoRequested: false,
    editingIndex: -1
}

const CreateExercise: NextPage<Props> = (props: Props) => {
    const [exercise, setExercise] = useState<Exercise>(props.exercise || defaultExercise)
    const [error, setError] = useState<string>("")
    const [createSetForm, setCreateSetForm] = useState<SetForm>(defaultSetForm)
    const [createSetFormOpen, setCreateSetFormOpen] = useState<boolean>(false)
    const [editing, setEditing] = useState<boolean>(false)

    useEffect(() => {
        setExercise(props.exercise || defaultExercise)
    }, [props.exercise])

    const handleNameChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setExercise({ ...exercise, name: e.target.value })
    }

    const saveExercise = async () => {
        try {
            let program: Program
            if (exercise.id.length == 0) {
                program = await Api.createExercise(props.programId, props.weekId, props.day.id, exercise.name, "")
                const exerciseId: string | undefined = program
                    .weeks.find(week => week.id == props.weekId)
                    ?.days.find(day => day.id == props.day.id)
                    ?.exercises.at(-1)?.id

                if (exerciseId == undefined) {
                    return setError("Unexpected error. Please try again")
                }

                for (let i = 0; i < exercise.sets.length; i++) {
                    program = await Api.createSet(
                        props.programId,
                        props.weekId,
                        props.day.id,
                        exerciseId,
                        exercise.sets[i].reps,
                        exercise.sets[i].rpe,
                        exercise.sets[i].percentage,
                        exercise.sets[i].weight,
                        exercise.sets[i].percentageReference,
                        exercise.sets[i].videoRequested
                    )
                }

            } else {
                program = await Api.updateExercise(props.programId, props.weekId, props.day.id, exercise.id, exercise)
            }
            props.setProgram(program)
            props.setOpen(false)
        } catch (error: any) {
            if (error instanceof NeedLogin) {
                document.location.href = "/login"
            } else if (error instanceof ApiError) {
                if (error.message.includes("must not be blank")) {
                    setError("Exercise name must not be blank")
                } else {
                    setError("Unexpected error. Please try again later")
                }
            } else {
                setError("Unexpected error. Please try again later")
            }
        }
    }

    const saveSet = (e: React.FormEvent) => {
        e.preventDefault()
        if (createSetForm.editingIndex == -1) {
            setExercise({
                ...exercise, sets: exercise.sets.concat([{
                    id: "",
                    reps: createSetForm.reps,
                    completedReps: 0,
                    percentage: createSetForm.percentage,
                    percentageReference: createSetForm.percentageReference,
                    weight: createSetForm.weight,
                    rpe: createSetForm.type == "RPE" ? createSetForm.rpe : -1,
                    videoRef: "",
                    videoRequested: createSetForm.videoRequested
                }])
            })
        } else {
            const editedSet = exercise.sets[createSetForm.editingIndex]
            editedSet.reps = createSetForm.reps
            editedSet.percentage = createSetForm.percentage
            editedSet.percentageReference = createSetForm.percentageReference
            editedSet.weight = createSetForm.weight
            editedSet.rpe = createSetForm.type == "RPE" ? createSetForm.rpe : -1
            editedSet.videoRequested = createSetForm.videoRequested
            editedSet.completedReps = -1 // resets athlete progress

            const updatedSets = exercise.sets
            updatedSets[createSetForm.editingIndex] = editedSet

            setExercise({
                ...exercise,
                sets: updatedSets
            })
        }
        setCreateSetFormOpen(false)
        setCreateSetForm(defaultSetForm)
    }

    const handleSetChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
        setCreateSetForm({ ...createSetForm, [e.target.name]: e.target.value })
    }

    const editExistingSet = (set: Set, index: number) => {
        setEditing(false)
        setCreateSetForm({
            type: set.rpe == -1 ? "PERCENTAGE" : "RPE",
            reps: set.reps,
            rpe: set.rpe,
            videoRequested: set.videoRequested,
            weight: set.weight,
            percentageReference: set.percentageReference,
            percentage: set.percentage,
            editingIndex: index
        })

        setCreateSetFormOpen(true)
    }

    const deleteSet = (index: number) => {
        const updatedSets = exercise.sets
        updatedSets.splice(index, 1)
        setExercise({
            ...exercise,
            sets: updatedSets
        })
    }

    const modalContent = () => {
        return (
            <div className="flex flex-col items-center">
                <h1 className="text-2xl underline text-center">
                    {
                        exercise.id == "" ?
                            "Create Exercise"
                            :
                            "Edit Exercise"
                    }
                </h1>
                <div className="flex flex-col items-center mt-3 w-fit">
                    <form id="name-form">
                        <label htmlFor="exerciseName">Name: </label>
                        <input required={true} value={exercise.name} onChange={handleNameChange} name="exerciseName" type="text" className="border border-black rounded-md px-2" />
                    </form>
                    <div className="flex flex-row mt-5 items-end w-full justify-between border-b border-b-black">
                        <h2 className="h-fit hover:cursor-pointer" onClick={() => setEditing(!editing)}>{editing ? "Done" : "Edit"}</h2>
                        <h2 className="text-xl w-fit font-medium">Sets</h2>
                        <svg onClick={() => { setCreateSetFormOpen(true) }} xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className="w-6 h-6 mt-1.5 hover:cursor-pointer">
                            <path strokeLinecap="round" strokeLinejoin="round" d="M12 4.5v15m7.5-7.5h-15" />
                        </svg>
                    </div>
                    {createSetFormOpen &&
                        <form className="w-fit flex rounded-md flex-col justify-center items-center border border-black py-6 px-8 mt-3" onSubmit={saveSet}>
                            <div className="w-full toggle">
                                <input type="radio" name="type" value="RPE" id="rpeToggle" onChange={handleSetChange} />
                                <label htmlFor="rpeToggle">RPE</label>
                                <input type="radio" name="type" value="PERCENTAGE" id="percentageToggle" defaultChecked onChange={handleSetChange} />
                                <label htmlFor="percentageToggle">Percentage</label>
                            </div>
                            <div className="flex flex-col">
                                <label className="mt-1" htmlFor="reps">Reps</label>
                                <input className="w-fit border-black border rounded-md py-0.5 px-2" type="number" name="reps" onChange={handleSetChange} placeholder="Reps" value={createSetForm.reps} />
                            </div>
                            {
                                createSetForm.type == "PERCENTAGE" ?
                                    <div className="flex flex-col">
                                        <label className="mt-1" htmlFor="percentage">Percentage</label>
                                        <div className="relative rounded-md shadow-sm">
                                            <input id="percentage" className="block w-fit border-black border rounded-md py-0.5 px-2" type="number" name="percentage" onChange={handleSetChange} placeholder="Percentage" value={createSetForm.percentage} />
                                            <div className="absolute inset-y-0 right-0 flex items-center pt-0.5 border-l border-l-black">
                                                <div id="currency" className="h-full rounded-md border-transparent bg-transparent py-0 px-4">
                                                    <p>%</p>
                                                </div>
                                            </div>
                                        </div>
                                        <label className="mt-1" htmlFor="percentageReference">Percentage Reference</label>
                                        <select className="enabled:border-black enabled:pl-1 enabled:mb-1.5 enabled:border enabled:py-1 enabled:rounded-md focus:outline-none" onChange={handleSetChange} name="percentageReference" id="percentageReference" value={createSetForm.percentageReference} >
                                            <option value={PercentageOptions.Squat}>Squat</option>
                                            <option value={PercentageOptions.Bench}>Bench</option>
                                            <option value={PercentageOptions.Deadlift}>Deadlift</option>
                                        </select>
                                    </div>
                                    :
                                    <div className="flex flex-col">
                                        <label htmlFor="weight mt-1">Weight</label>
                                        <div className="relative rounded-md shadow-sm">
                                            <input className="block w-fit border-black border rounded-md py-0.5 px-2" type="number" name="weight" onChange={handleSetChange} placeholder="Weight" value={createSetForm.weight} />
                                            <div className="absolute inset-y-0 right-0 flex items-center pt-0.5 border-l border-l-black">
                                                <div id="currency" className="h-full rounded-md border-transparent bg-transparent py-0 px-4">
                                                    <p>kg</p>
                                                </div>
                                            </div>
                                        </div>
                                        <label htmlFor="weight mt-1">RPE</label>
                                        <div className="relative rounded-md shadow-sm">
                                            <input className="block w-fit border-black border rounded-md py-0.5 px-2" type="number" name="rpe" onChange={handleSetChange} placeholder="RPE" value={createSetForm.rpe} />
                                        </div>
                                    </div>
                            }
                            <div className="flex flex-row w-full justify-between mt-1">
                                <label htmlFor="default-toggle">Request video?</label>
                                <label htmlFor="small-toggle" className="inline-flex relative items-center mb-5 cursor-pointer">
                                    <input onChange={() => setCreateSetForm({ ...createSetForm, videoRequested: !createSetForm.videoRequested })} type="checkbox" checked={createSetForm.videoRequested} value="" id="small-toggle" className="sr-only peer" />
                                    <div className="w-9 h-5 bg-black rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-4 after:w-4 after:transition-all dark:border-gray-600 peer-checked:bg-[#A020F0]"></div>
                                </label>
                            </div>
                            <div className="flex flex-row">
                                <input onClick={() => setCreateSetFormOpen(false)} type="button" className="mr-3 disabled:opacity-80 disabled:cursor-progress purple-bg mt-6 cursor-pointer py-1 px-6 rounded-md text-white" value="Cancel" />
                                <input type="submit" className="disabled:opacity-80 disabled:cursor-progress purple-bg mt-6 cursor-pointer py-1 px-6 rounded-md text-white" value={createSetForm.editingIndex == -1 ? "Add" : "Edit"} />
                            </div>
                        </form>
                    }
                    {
                        exercise.sets.map((set, index) => {
                            return (
                                <div key={index} className="flex flex-row w-full justify-center">
                                    {
                                        editing &&
                                        <div className="flex-1">
                                            <svg onClick={() => deleteSet(index)} xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className="w-6 h-6 hover:cursor-pointer">
                                                <path strokeLinecap="round" strokeLinejoin="round" d="M19.5 12h-15" />
                                            </svg>
                                        </div>
                                    }
                                    <button onClick={() => editExistingSet(set, index)} className={editing ? "hover:cursor-pointer" : "hover:cursor-default"} disabled={!editing}>
                                        <h1>{set.reps} reps {set.rpe == -1 ? `at ${set.percentage}%` : `at ${set.weight}kg with RPE ${set.rpe}`}</h1>
                                    </button>
                                    {
                                        // placeholder element to keep the centering 
                                        editing &&
                                        <button onClick={() => editExistingSet(set, index)} className={"flex-1 flex justify-end " + (editing ? "hover:cursor-pointer" : "hover:cursor-default")} disabled={!editing}>
                                            Edit
                                        </button>
                                    }
                                </div>
                            )
                        })
                    }
                </div>
                {error.length != 0 && <p className="text-center break-words min-w-full w-0 text-red-600 mt-2"> {error} </p>}
                <div className="flex flex-row justify-end mt-6">
                    <input type="button" onClick={() => props.setOpen(false)} value="Cancel" className="purple-bg text-white rounded-md px-6 py-1 cursor-pointer" />
                    <input type="button" onClick={saveExercise} value="Save" className="purple-bg text-white rounded-md px-6 py-1 cursor-pointer ml-4" />
                </div>
            </div >
        )
    }


    return (
        <Modal open={props.open} setOpen={props.setOpen} >
            {modalContent()}
        </Modal>
    )
}

export default CreateExercise

