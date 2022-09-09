import { NextPage } from "next";
import Link from "next/link";
import { useRouter } from "next/router";
import { useEffect, useState } from "react";
import Accordion from "../../../components/Accordion";
import Dropdown from "../../../components/Dropdown";
import Modal from "../../../components/Modal";
import NavBar from "../../../components/NavBar";
import { useUserContext } from "../../../context/UserContext";
import Api from '../../../utils/api'
import { NeedLogin } from "../../../utils/api.errors";
import { Day, Program, UserType } from "../../../utils/api.types";

const ProgramPage: NextPage = () => {
    const router = useRouter()
    const id = router.query.id as string
    const [program, setProgram] = useState<Program | null>(null)
    const [loading, setLoading] = useState<boolean>(true)
    const [error, setError] = useState<string>("")
    const [createWeekError, setCreateWeekError] = useState<string>("")
    const { user, setUser } = useUserContext()
    const [weekNotes, setWeekNotes] = useState<{ [key: string]: string }>({})
    const [weekNoteEditing, setWeekNoteEditing] = useState<{ [key: string]: boolean }>({})
    const [saveNoteError, setSaveNoteError] = useState<{ [key: string]: string }>({})
    const [createDayError, setCreateDayError] = useState<{ [key: string]: string }>({})
    const [dayNotes, setDayNotes] = useState<{ [key: string]: string }>({})
    const [dayNoteEditing, setDayNoteEditing] = useState<{ [key: string]: boolean }>({})

    const fetchData = async (): Promise<Program | null> => {
        try {
            const fetchedProgram: Program = await Api.getProgram(id as string)
            setProgram(fetchedProgram)
            setUser(await Api.getUserInfo())
            return fetchedProgram
        } catch (error: any) {
            if (error instanceof NeedLogin) {
                window.location.href = "/login"
            } else if (error.response.status == 404) {
                setError("Program not found. Please return to the dashboard and try again.")
            } else {
                setError("Unexpected error. Please return to the dashboard and try again")
            }
            setProgram(null)
        }
        return null
    }

    useEffect(() => {
        if (router.isReady) {
            fetchData().then((fetchedProgram: Program | null) => {
                if (fetchedProgram) {
                    const weekNotes: { [key: string]: string } = {}
                    const dayNotes: { [key: string]: string } = {}
                    fetchedProgram.weeks.forEach(week => {
                        setWeekNoteEditing({ ...weekNoteEditing, [week.id]: false })
                        weekNotes[`${week.id}-coach-note`] = week.coachNotes
                        weekNotes[`${week.id}-athlete-note`] = week.athleteNotes
                        week.days.forEach(day => {
                            setDayNoteEditing({ ...dayNoteEditing, [day.id]: false })
                            dayNotes[`${day.id}-coach-note`] = day.coachNotes
                            dayNotes[`${day.id}-athlete-note`] = day.athleteNotes
                        })
                    })
                    setDayNotes(dayNotes)
                    setWeekNotes(weekNotes)
                }
            }).finally(() => {
                setLoading(false)
            })
        }
    }, [router.isReady])

    const createNewWeek = async () => {
        try {
            const updatedProgram = await Api.createWeek(program!.id, "")
            setProgram(updatedProgram)
            setCreateWeekError("")
        } catch (error: any) {
            if (error instanceof NeedLogin) {
                window.location.href = "/login"
            } else if (error.response.status == 404) {
                setProgram(null)
                setError("Unexpected error. Please return to the dashboard and try again")
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
            if (user!.userType == UserType.COACH) {
                updatedProgram = await Api.updateWeekCoachNote(
                    program!.id,
                    weekId,
                    weekNotes[`${weekId}-coach-note`]
                )

                setWeekNotes({ ...weekNotes, [`${weekId}-coach-note`]: updatedProgram.weeks.find(week => week.id == weekId)!.coachNotes })
            } else {
                updatedProgram = await Api.updateWeekAthleteNote(
                    program!.id,
                    weekId,
                    weekNotes[`${weekId}-athlete-note`]
                )
                setWeekNotes({ ...weekNotes, [`${weekId}-athlete-note`]: updatedProgram.weeks.find(week => week.id == weekId)!.athleteNotes })
            }
            setWeekNoteEditing({ ...weekNoteEditing, [weekId]: false })
            setProgram(updatedProgram)
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

    const createNewDay = async (e: React.MouseEvent<HTMLButtonElement>) => {
        const weekId = e.currentTarget.id.split("-")[0]
        try {
            const updatedProgram = await Api.createDay(program!.id, weekId, "")
            setProgram(updatedProgram)
            setCreateDayError({ ...createDayError, [weekId]: "" })
        } catch (error: any) {
            if (error instanceof NeedLogin) {
                window.location.href = "/login"
            } else if (error.response.status == 404) {
                setCreateDayError({ ...createDayError, [weekId]: "Program not found. Please return to the dashboard and try again." })
            } else {
                setCreateDayError({ ...createDayError, [weekId]: "Unexpected error. Please return to the dashboard and try again" })
            }
        }
    }

    const generateAccordion = (): { heading: string, body: React.ReactElement }[] => {
        return program!.weeks.map((week, index) => {
            return {
                heading: `Week ${index + 1}`,
                body: (
                    <div className="flex flex-row divide-x divide-black">
                        <form className="pr-12" onSubmit={updateNote} id={`${week.id}-notes`}>
                            <div className="flex flex-col">
                                <label className="mr-0.5">Coach Notes: </label>
                                {
                                    weekNoteEditing[week.id] && user!.userType == UserType.COACH ?
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
                                            {user!.userType == UserType.COACH &&
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
                                    weekNoteEditing[week.id] && user!.userType == UserType.ATHLETE ?
                                        <div className="flex flex-col">
                                            <textarea onChange={handleNoteChange} id={`${week.id}-athlete-note`} className="pl-2 bg-white border-black border rounded-md resize-none h-24" value={weekNotes[`${week.id}-athlete-note`] || ""} />
                                            <div className="flex flex-row">
                                                <input onClick={() => setWeekNoteEditing({ ...weekNoteEditing, [week.id]: false })} type="button" value="Cancel" className="purple-bg rounded-md text-white py-0.5 px-5 hover:cursor-pointer" />
                                                <input type="submit" value="Save" className="purple-bg rounded-md text-white py-0.5 px-5 ml-2 hover:cursor-pointer" />
                                            </div>
                                            <p className="text-red">{saveNoteError[week.id]?.length != 0 && saveNoteError[week.id]}</p>
                                        </div>
                                        :
                                        <div className="flex flex-row justify-between">
                                            <textarea value={week.athleteNotes || "None"} className="bg-white resize-none h-fit" />
                                            {user!.userType == UserType.ATHLETE &&
                                                <svg onClick={() => setWeekNoteEditing({ ...weekNoteEditing, [week.id]: !weekNoteEditing[week.id] })} xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className="w-6 h-6 pt-0.5 ml-0.5 hover:cursor-pointer">
                                                    <path strokeLinecap="round" strokeLinejoin="round" d="M16.862 4.487l1.687-1.688a1.875 1.875 0 112.652 2.652L10.582 16.07a4.5 4.5 0 01-1.897 1.13L6 18l.8-2.685a4.5 4.5 0 011.13-1.897l8.932-8.931zm0 0L19.5 7.125M18 14v4.75A2.25 2.25 0 0115.75 21H5.25A2.25 2.25 0 013 18.75V8.25A2.25 2.25 0 015.25 6H10" />
                                                </svg>
                                            }
                                        </div>
                                }
                            </div>
                        </form>
                        <div className="px-12">
                            <div className="flex flex-row justify-between items-end border-b border-b-black pb-0.5">
                                <h3 className="h-fit">Edit</h3>
                                <h2 className="text-xl w-fit font-medium mx-8">Days</h2>
                                <button onClick={createNewDay} id={`${week.id}-new-week`}>
                                    <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className="w-5 h-5 mb-0.5 hover:cursor-pointer">
                                        <path strokeLinecap="round" strokeLinejoin="round" d="M12 4.5v15m7.5-7.5h-15" />
                                    </svg>
                                </button>
                            </div>
                            {
                                createDayError[week.id]?.length != 0 &&
                                <p className="text-red-300">{createDayError[week.id]}</p>
                            }
                            <Accordion items={generateDaysContent(week.id, week.days)} animationTime={200} loaded={loading} />
                        </div>
                    </div>
                )
            }
        })
    }

    const handleDayNoteChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
        setDayNotes({ ...dayNotes, [e.target.id]: e.target.value })
    }

    const saveDayNote = async (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault()
        let updatedProgram: Program
        const [weekId, dayId] = e.currentTarget.id.split("-")

        try {
            if (user!.userType == UserType.COACH) {
                updatedProgram = await Api.updateDayCoachNote(
                    program!.id,
                    weekId,
                    dayId,
                    dayNotes[`${dayId}-coach-note`]
                )

                setDayNotes({ ...dayNotes, [`${dayId}-coach-note`]: updatedProgram.weeks.find(week => week.id == weekId)!.days.find(day => dayId == day.id)!.coachNotes })
            } else {
                updatedProgram = await Api.updateDayAthleteNote(
                    program!.id,
                    weekId,
                    dayId,
                    dayNotes[`${weekId}-athlete-note`]
                )
                setDayNotes({ ...dayNotes, [`${dayId}-athlete-note`]: updatedProgram.weeks.find(week => week.id == weekId)!.days.find(day => dayId == day.id)!.athleteNotes })
            }
            setDayNoteEditing({ ...dayNoteEditing, [dayId]: false })
            setProgram(updatedProgram)
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

    const generateDaysContent = (weekId: string, days: Day[]): { heading: string, body: React.ReactElement }[] => {
        return days.map((day, index) => {
            return {
                heading: `Day ${index + 1}`,
                body: (
                    <div>
                        <form className="pr-12" onSubmit={saveDayNote} id={`${weekId}-${day.id}-notes`}>
                            <div className="flex flex-col">
                                <label className="mr-0.5">Coach Notes: </label>
                                {
                                    dayNoteEditing[day.id] && user!.userType == UserType.COACH ?
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
                                            {user!.userType == UserType.COACH &&
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
                                    dayNoteEditing[day.id] && user!.userType == UserType.ATHLETE ?
                                        <div className="flex flex-col">
                                            <textarea onChange={handleDayNoteChange} id={`${day.id}-athlete-note`} className="pl-2 bg-white border-black border rounded-md resize-none h-24" value={dayNotes[`${day.id}-athlete-note`] || ""} />
                                            <div className="flex flex-row">
                                                <input onClick={() => setDayNoteEditing({ ...dayNoteEditing, [day.id]: false })} type="button" value="Cancel" className="purple-bg rounded-md text-white py-0.5 px-5 hover:cursor-pointer" />
                                                <input type="submit" value="Save" className="purple-bg rounded-md text-white py-0.5 px-5 ml-2 hover:cursor-pointer" />
                                            </div>
                                            <p className="text-red">{saveNoteError[`${weekId}-${day.id}`]?.length != 0 && saveNoteError[`${weekId}-${day.id}`]}</p>
                                        </div>
                                        :
                                        <div className="flex flex-row justify-between">
                                            <textarea value={day.athleteNotes || "None"} className="bg-white resize-none h-fit" />
                                            {user!.userType == UserType.ATHLETE &&
                                                <svg onClick={() => setDayNoteEditing({ ...dayNoteEditing, [day.id]: !dayNoteEditing[day.id] })} xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className="w-6 h-6 pt-0.5 ml-0.5 hover:cursor-pointer">
                                                    <path strokeLinecap="round" strokeLinejoin="round" d="M16.862 4.487l1.687-1.688a1.875 1.875 0 112.652 2.652L10.582 16.07a4.5 4.5 0 01-1.897 1.13L6 18l.8-2.685a4.5 4.5 0 011.13-1.897l8.932-8.931zm0 0L19.5 7.125M18 14v4.75A2.25 2.25 0 0115.75 21H5.25A2.25 2.25 0 013 18.75V8.25A2.25 2.25 0 015.25 6H10" />
                                                </svg>
                                            }
                                        </div>
                                }
                            </div>
                        </form>
                    </div>
                )
            }
        })
    }

    return (
        <div className="w-100 flex flex-col">
            <NavBar loading={loading} />
            {
                (!loading && program) &&
                <div className="flex flex-col ">
                    <div className="flex flex-col items-center">
                        <h1 className="text-3xl w-fit font-medium border-b-black border-b-2 pb-0.5 px-1">{program!.name}</h1>
                        <h2 className="text-xl w-fit">{program.description}</h2>
                    </div>

                    <div className="flex flex-col items-center mt-8">
                        <div className="w-fit">
                            <div className="flex flex-row justify-between items-end border-b border-b-black pb-1">
                                <h2 className="text-xl h-fit">Edit</h2>
                                <h2 className="text-2xl w-fit font-medium">Weeks</h2>
                                <svg onClick={createNewWeek} xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className="w-6 h-6 mt-1.5 hover:cursor-pointer">
                                    <path strokeLinecap="round" strokeLinejoin="round" d="M12 4.5v15m7.5-7.5h-15" />
                                </svg>
                            </div>
                            {createWeekError.length != 0 && <p className="text-red">{createWeekError}</p>}
                            <div className="mt-3">
                                {
                                    program.weeks.length > 0 ?
                                        <Accordion loaded={loading} items={generateAccordion()} animationTime={200} />
                                        :
                                        <p>No weeks created</p>
                                }
                            </div>
                        </div>
                    </div>
                </div>
            }
            {
                error.length != 0 && (
                    <div>
                        <p>{error}</p>
                        <Link href="/dashboard">Return to dashboard</Link>
                    </div>
                )
            }
        </div>
    )
}

export default ProgramPage

