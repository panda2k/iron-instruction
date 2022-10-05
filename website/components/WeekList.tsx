import { NextPage } from "next"
import { useEffect, useState } from "react"
import { NeedLogin } from "../utils/api.errors"
import { Program, UserType, Week } from "../utils/api.types"
import Api from '../utils/api'
import Accordion from "./Accordion"
import DayList from "./DayList"

type Props = {
    weeks: Week[]
    setProgram: (program: Program | null) => void
    userType: UserType
    programId: string
    setPageError: (error: string) => void
    loading: boolean
}

const WeekList: NextPage<Props> = (props: Props) => {
    const [createWeekError, setCreateWeekError] = useState<string>("")
    const [weekNotes, setWeekNotes] = useState<{ [key: string]: string }>({})
    const [weekNoteEditing, setWeekNoteEditing] = useState<{ [key: string]: boolean }>({})
    const [saveNoteError, setSaveNoteError] = useState<{ [key: string]: string }>({})
    const [editingWeeks, setEditingWeeks] = useState<boolean>(false)

    useEffect(() => {
        const weekNotes: { [key: string]: string } = {}
        props.weeks.forEach(week => {
            setWeekNoteEditing({ ...weekNoteEditing, [week.id]: false })
            weekNotes[`${week.id}-coach-note`] = week.coachNotes
            weekNotes[`${week.id}-athlete-note`] = week.athleteNotes
        })
        setWeekNotes(weekNotes)
    }, [])

    const deleteWeek = async (weekId: string) => {
        const updatedProgram: Program = await Api.deleteWeek(props.programId, weekId)
        props.setProgram(updatedProgram)
    }

    const createNewWeek = async () => {
        try {
            const updatedProgram = await Api.createWeek(props.programId, "")
            props.setProgram(updatedProgram)
            setCreateWeekError("")
        } catch (error: any) {
            if (error instanceof NeedLogin) {
                window.location.href = "/login"
            } else if (error.response.status == 404) {
                props.setProgram(null)
                props.setPageError("Unexpected error. Please return to the dashboard and try again")
            } else {
                setCreateWeekError("Unexpected error. Please try again")
            }
        }
    }

    const handleNoteChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
        setWeekNotes({ ...weekNotes, [e.target.id]: e.target.value })
    }

    const updateNote = async (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault()
        let updatedProgram: Program
        const weekId = e.currentTarget.id.split("-")[0]
        try {
            if (props.userType == UserType.COACH) {
                updatedProgram = await Api.updateWeekCoachNote(
                    props.programId,
                    weekId,
                    weekNotes[`${weekId}-coach-note`]
                )

                setWeekNotes({ ...weekNotes, [`${weekId}-coach-note`]: updatedProgram.weeks.find(week => week.id == weekId)!.coachNotes })
            } else {
                updatedProgram = await Api.updateWeekAthleteNote(
                    props.programId,
                    weekId,
                    weekNotes[`${weekId}-athlete-note`]
                )
                setWeekNotes({ ...weekNotes, [`${weekId}-athlete-note`]: updatedProgram.weeks.find(week => week.id == weekId)!.athleteNotes })
            }
            setWeekNoteEditing({ ...weekNoteEditing, [weekId]: false })
            props.setProgram(updatedProgram)
            setSaveNoteError({ ...saveNoteError, [weekId]: "" })
        } catch (error: any) {
            if (error instanceof NeedLogin) {
                window.location.href = "/login"
            } else if (error.response.status == 404) {
                setSaveNoteError({ ...saveNoteError, [weekId]: "Program not found. Please return to the dashboard and try again." })
            } else {
                setSaveNoteError({ ...saveNoteError, [weekId]: "Unexpected error. Please return to the dashboard and try again" })
            }
        }
    }

    const generateAccordion = (): { heading: string, body: React.ReactElement, headerExtras?: React.ReactElement }[] => {
        return props.weeks.map((week, index) => {
            return {
                ...(editingWeeks) && {
                    headerExtras: (
                        <svg onClick={() => deleteWeek(week.id)} xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className="w-6 h-6 mr-3 mt-2 -mb-0.5 hover:cursor-pointer">
                            <path strokeLinecap="round" strokeLinejoin="round" d="M19.5 12h-15" />
                        </svg>
                    )
                },
                heading: `Week ${index + 1}`,
                body: (
                    <div className="flex flex-row divide-x divide-black">
                        <form className="pr-12" onSubmit={updateNote} id={`${week.id}-notes`}>
                            <div className="flex flex-col">
                                <label className="mr-0.5">Coach Notes: </label>
                                {
                                    weekNoteEditing[week.id] && props.userType == UserType.COACH ?
                                        <div className="flex flex-col">
                                            <textarea onChange={handleNoteChange} id={`${week.id}-coach-note`} className="pl-2 bg-white border-black border rounded-md resize-none h-24" value={weekNotes[`${week.id}-coach-note`] || ""} />
                                            <div className="flex flex-row justify-between mx-5 mb-3 mt-1.5">
                                                <input onClick={() => setWeekNoteEditing({ ...weekNoteEditing, [week.id]: false })} type="button" value="Cancel" className="purple-bg rounded-md text-white py-0.5 px-5 hover:cursor-pointer" />
                                                <input type="submit" value="Save" className="purple-bg rounded-md text-white py-0.5 px-5 ml-2 hover:cursor-pointer" />
                                            </div>
                                            <p className="text-red-500">{saveNoteError[week.id]?.length != 0 && saveNoteError[week.id]}</p>
                                        </div>
                                        :
                                        <div className="flex flex-row justify-between">
                                            <textarea value={week.coachNotes || "None"} className="bg-white resize-none h-fit" />
                                            {props.userType == UserType.COACH &&
                                                <svg onClick={() => setWeekNoteEditing({ ...weekNoteEditing, [week.id]: !weekNoteEditing[week.id] })} xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className="w-6 h-6 pt-0.5 ml-0.5 hover:cursor-pointer">
                                                    <path strokeLinecap="round" strokeLinejoin="round" d="M16.862 4.487l1.687-1.688a1.875 1.875 0 112.652 2.652L10.582 16.07a4.5 4.5 0 01-1.897 1.13L6 18l.8-2.685a4.5 4.5 0 011.13-1.897l8.932-8.931zm0 0L19.5 7.125M18 14v4.75A2.25 2.25 0 0115.75 21H5.25A2.25 2.25 0 013 18.75V8.25A2.25 2.25 0 015.25 6H10" />
                                                </svg>
                                            }
                                        </div>
                                }
                            </div>
                            <div className="flex flex-col">
                                <label className="mr-0.5">Athlete Notes: </label>
                                {
                                    weekNoteEditing[week.id] && props.userType == UserType.ATHLETE ?
                                        <div className="flex flex-col">
                                            <textarea onChange={handleNoteChange} id={`${week.id}-athlete-note`} className="pl-2 bg-white border-black border rounded-md resize-none h-24" value={weekNotes[`${week.id}-athlete-note`] || ""} />
                                            <div className="flex flex-row mt-1.5">
                                                <input onClick={() => setWeekNoteEditing({ ...weekNoteEditing, [week.id]: false })} type="button" value="Cancel" className="purple-bg rounded-md text-white py-0.5 px-5 hover:cursor-pointer" />
                                                <input type="submit" value="Save" className="purple-bg rounded-md text-white py-0.5 px-5 ml-2 hover:cursor-pointer" />
                                            </div>
                                            <p className="text-red">{saveNoteError[week.id]?.length != 0 && saveNoteError[week.id]}</p>
                                        </div>
                                        :
                                        <div className="flex flex-row justify-between">
                                            <textarea value={week.athleteNotes || "None"} className="bg-white resize-none h-fit" />
                                            {props.userType == UserType.ATHLETE &&
                                                <svg onClick={() => setWeekNoteEditing({ ...weekNoteEditing, [week.id]: !weekNoteEditing[week.id] })} xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className="w-6 h-6 pt-0.5 ml-0.5 hover:cursor-pointer">
                                                    <path strokeLinecap="round" strokeLinejoin="round" d="M16.862 4.487l1.687-1.688a1.875 1.875 0 112.652 2.652L10.582 16.07a4.5 4.5 0 01-1.897 1.13L6 18l.8-2.685a4.5 4.5 0 011.13-1.897l8.932-8.931zm0 0L19.5 7.125M18 14v4.75A2.25 2.25 0 0115.75 21H5.25A2.25 2.25 0 013 18.75V8.25A2.25 2.25 0 015.25 6H10" />
                                                </svg>
                                            }
                                        </div>
                                }
                            </div>
                        </form>
                        <DayList userType={props.userType} setProgram={props.setProgram} programId={props.programId} days={week.days} weekId={week.id} loading={props.loading} />
                    </div>
                )
            }
        })
    }
    return (
        <div className="w-fit">
            <div className={"flex flex-row items-end border-b border-b-black pb-1" + (props.userType == UserType.COACH ? " justify-between" : " justify-center")}>
                {props.userType == UserType.COACH &&
                    <button onClick={() => setEditingWeeks(!editingWeeks)} className="hover:cursor-pointer">
                        <h2 className="text-xl h-fit">{editingWeeks ? "Done" : "Edit"}</h2>
                    </button>
                }
                <h2 className="text-2xl w-fit font-medium">Weeks</h2>
                {props.userType == UserType.COACH &&
                    <svg onClick={createNewWeek} xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className="w-6 h-6 mt-1.5 hover:cursor-pointer">
                        <path strokeLinecap="round" strokeLinejoin="round" d="M12 4.5v15m7.5-7.5h-15" />
                    </svg>
                }
            </div>

            {createWeekError.length != 0 && <p className="text-red">{createWeekError}</p>}
            <div className="mt-3">
                {
                    props.weeks.length > 0 ?
                        <Accordion loaded={props.loading} items={generateAccordion()} animationTime={200} />
                        :
                        <p>No weeks created</p>
                }
            </div>
        </div>
    )
}

export default WeekList

