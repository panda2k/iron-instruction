import { NextPage } from "next";
import { useEffect, useState } from "react";
import { NeedLogin } from "../utils/api.errors";
import { Day, Program, UserType } from "../utils/api.types"
import Api from "../utils/api"
import ExerciseList from "./ExerciseList";
import Accordion from "./Accordion";

type Props = {
    userType: UserType
    setProgram: (program: Program) => void
    programId: string
    days: Day[]
    weekId: string
    loading: boolean
}

const DayList: NextPage<Props> = (props: Props) => {
    const [createDayError, setCreateDayError] = useState<string>("")
    const [dayNotes, setDayNotes] = useState<{ [key: string]: string }>({})
    const [dayNoteEditing, setDayNoteEditing] = useState<{ [key: string]: boolean }>({})
    const [saveNoteError, setSaveNoteError] = useState<{ [key: string]: string }>({})
    const [editingDays, setEditingDays] = useState<boolean>(false)

    useEffect(() => {
        const dayNotes: { [key: string]: string } = {}
        props.days.forEach(day => {
            setDayNoteEditing({ ...dayNoteEditing, [day.id]: false })
            dayNotes[`${day.id}-coach-note`] = day.coachNotes
            dayNotes[`${day.id}-athlete-note`] = day.athleteNotes
        })
        setDayNotes(dayNotes)
    }, [])

    const handleDayNoteChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
        setDayNotes({ ...dayNotes, [e.target.id]: e.target.value })
    }

    const createNewDay = async (e: React.MouseEvent<HTMLButtonElement>) => {
        const weekId = e.currentTarget.id.split("-")[0]
        try {
            const updatedProgram = await Api.createDay(props.programId, weekId, "")
            props.setProgram(updatedProgram)
            setCreateDayError("")
        } catch (error: any) {
            if (error instanceof NeedLogin) {
                window.location.href = "/login"
            } else if (error.response.status == 404) {
                setCreateDayError("Program not found. Please return to the dashboard and try again.")
            } else {
                setCreateDayError("Unexpected error. Please return to the dashboard and try again")
            }
        }
    }

    const saveDayNote = async (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault()
        let updatedProgram: Program
        const [weekId, dayId] = e.currentTarget.id.split("-")

        try {
            if (props.userType == UserType.COACH) {
                updatedProgram = await Api.updateDayCoachNote(
                    props.programId,
                    weekId,
                    dayId,
                    dayNotes[`${dayId}-coach-note`]
                )

                setDayNotes({ ...dayNotes, [`${dayId}-coach-note`]: updatedProgram.weeks.find(week => week.id == weekId)!.days.find(day => dayId == day.id)!.coachNotes })
            } else {
                updatedProgram = await Api.updateDayAthleteNote(
                    props.programId,
                    weekId,
                    dayId,
                    dayNotes[`${dayId}-athlete-note`]
                )
                setDayNotes({ ...dayNotes, [`${dayId}-athlete-note`]: updatedProgram.weeks.find(week => week.id == weekId)!.days.find(day => dayId == day.id)!.athleteNotes })
            }
            setDayNoteEditing({ ...dayNoteEditing, [dayId]: false })
            props.setProgram(updatedProgram)
        } catch (error: any) {
            if (error instanceof NeedLogin) {
                window.location.href = "/login"
            } else if (error.response.status == 404) {
                setSaveNoteError({ ...saveNoteError, [`${weekId}-${dayId}`]: "Program not found. Please return to the dashboard and try again." })
            } else {
                setSaveNoteError({ ...saveNoteError, [`${weekId}-${dayId}`]: "Unexpected error. Please return to the dashboard and try again" })
            }
        }
    }

    const deleteDay = async (dayId: string) => {
        const updatedProgram: Program = await Api.deleteDay(props.programId, props.weekId, dayId)
        props.setProgram(updatedProgram)
    }

    const generateDaysContent = (weekId: string, days: Day[]): { heading: string, body: React.ReactElement, headerExtras?: React.ReactElement }[] => {
        return days.map((day, index) => {
            return {
                ...(editingDays) && {
                    headerExtras: (
                        <svg onClick={() => deleteDay(day.id)} xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className="w-6 h-6 mr-3 mt-2 -mb-0.5 hover:cursor-pointer">
                            <path strokeLinecap="round" strokeLinejoin="round" d="M19.5 12h-15" />
                        </svg>
                    )
                },
                heading: `Day ${index + 1}`,
                body: (
                    <div className="flex flex-col">
                        <form className="pr-12" onSubmit={saveDayNote} id={`${weekId}-${day.id}-notes`}>
                            <div className="flex flex-col">
                                <label className="mr-0.5">Coach Notes: </label>
                                {
                                    dayNoteEditing[day.id] && props.userType == UserType.COACH ?
                                        <div className="flex flex-col">
                                            <textarea onChange={handleDayNoteChange} id={`${day.id}-coach-note`} className="pl-2 bg-white border-black border rounded-md resize-none h-24" value={dayNotes[`${day.id}-coach-note`] || ""} />
                                            <div className="flex flex-row justify-between mx-5 mb-3 mt-1.5">
                                                <input onClick={() => setDayNoteEditing({ ...dayNoteEditing, [day.id]: false })} type="button" value="Cancel" className="purple-bg rounded-md text-white py-0.5 px-5 hover:cursor-pointer" />
                                                <input type="submit" value="Save" className="purple-bg rounded-md text-white py-0.5 px-5 ml-2 hover:cursor-pointer" />
                                            </div>
                                            <p className="text-red">{saveNoteError[`${weekId}-${day.id}`]?.length != 0 && saveNoteError[`${weekId}-${day.id}`]}</p>
                                        </div>
                                        :
                                        <div className="flex flex-row justify-between">
                                            <textarea value={day.coachNotes || "None"} className="bg-white resize-none h-fit" />
                                            {props.userType == UserType.COACH &&
                                                <svg onClick={() => setDayNoteEditing({ ...dayNoteEditing, [day.id]: !dayNoteEditing[day.id] })} xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className="w-6 h-6 pt-0.5 ml-0.5 hover:cursor-pointer">
                                                    <path strokeLinecap="round" strokeLinejoin="round" d="M16.862 4.487l1.687-1.688a1.875 1.875 0 112.652 2.652L10.582 16.07a4.5 4.5 0 01-1.897 1.13L6 18l.8-2.685a4.5 4.5 0 011.13-1.897l8.932-8.931zm0 0L19.5 7.125M18 14v4.75A2.25 2.25 0 0115.75 21H5.25A2.25 2.25 0 013 18.75V8.25A2.25 2.25 0 015.25 6H10" />
                                                </svg>
                                            }
                                        </div>
                                }
                            </div>
                            <div className="flex flex-col">
                                <label className="mr-0.5">Athlete Notes: </label>
                                {
                                    dayNoteEditing[day.id] && props.userType == UserType.ATHLETE ?
                                        <div className="flex flex-col">
                                            <textarea onChange={handleDayNoteChange} id={`${day.id}-athlete-note`} className="pl-2 bg-white border-black border rounded-md resize-none h-24" value={dayNotes[`${day.id}-athlete-note`] || ""} />
                                            <div className="flex flex-row mt-1.5">
                                                <input onClick={() => setDayNoteEditing({ ...dayNoteEditing, [day.id]: false })} type="button" value="Cancel" className="purple-bg rounded-md text-white py-0.5 px-5 hover:cursor-pointer" />
                                                <input type="submit" value="Save" className="purple-bg rounded-md text-white py-0.5 px-5 ml-2 hover:cursor-pointer" />
                                            </div>
                                            <p className="text-red">{saveNoteError[`${weekId}-${day.id}`]?.length != 0 && saveNoteError[`${weekId}-${day.id}`]}</p>
                                        </div>
                                        :
                                        <div className="flex flex-row justify-between">
                                            <textarea value={day.athleteNotes || "None"} className="bg-white resize-none h-fit" />
                                            {props.userType == UserType.ATHLETE &&
                                                <svg onClick={() => setDayNoteEditing({ ...dayNoteEditing, [day.id]: !dayNoteEditing[day.id] })} xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className="w-6 h-6 pt-0.5 ml-0.5 hover:cursor-pointer">
                                                    <path strokeLinecap="round" strokeLinejoin="round" d="M16.862 4.487l1.687-1.688a1.875 1.875 0 112.652 2.652L10.582 16.07a4.5 4.5 0 01-1.897 1.13L6 18l.8-2.685a4.5 4.5 0 011.13-1.897l8.932-8.931zm0 0L19.5 7.125M18 14v4.75A2.25 2.25 0 0115.75 21H5.25A2.25 2.25 0 013 18.75V8.25A2.25 2.25 0 015.25 6H10" />
                                                </svg>
                                            }
                                        </div>
                                }
                            </div>
                        </form>
                        <ExerciseList userType={props.userType} setProgram={props.setProgram} day={day} weekId={weekId} programId={props.programId} />
                    </div>
                )
            }
        })
    }

    return (
        <div className="px-12 w-full">
            <div className={"flex flex-row items-end border-b border-b-black pb-0.5" + (props.userType == UserType.COACH ? " justify-between" : " justify-center")}>
                {props.userType == UserType.COACH &&
                    <button onClick={() => setEditingDays(!editingDays)} className="hover:cursor-pointer">
                        <h3 className="h-fit">{editingDays ? "Done" : "Edit"}</h3>
                    </button>
                }
                <h2 className="text-xl w-fit font-medium mx-8">Days</h2>
                {
                    props.userType == UserType.COACH &&
                    <button onClick={createNewDay} id={`${props.weekId}-new-day`}>
                        <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className="w-5 h-5 mb-0.5 hover:cursor-pointer">
                            <path strokeLinecap="round" strokeLinejoin="round" d="M12 4.5v15m7.5-7.5h-15" />
                        </svg>
                    </button>
                }
            </div>
            {
                createDayError.length != 0 &&
                <p className="text-red-300">{createDayError}</p>
            }

            <Accordion items={generateDaysContent(props.weekId, props.days)} animationTime={200} loaded={props.loading} />
        </div>
    )
}

export default DayList

