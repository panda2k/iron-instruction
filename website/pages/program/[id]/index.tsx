import { NextPage } from "next";
import Link from "next/link";
import { useRouter } from "next/router";
import { useEffect, useState } from "react";
import NavBar from "../../../components/NavBar";
import WeekList from "../../../components/WeekList";
import { useUserContext } from "../../../context/UserContext";
import Api from '../../../utils/api'
import { NeedLogin } from "../../../utils/api.errors";
import { Program, UserType } from "../../../utils/api.types";

const ProgramPage: NextPage = () => {
    const router = useRouter()
    const id = router.query.id as string
    const [program, setProgram] = useState<Program | null>(null)
    const [loading, setLoading] = useState<boolean>(true)
    const [error, setError] = useState<string>("")
    const { user, setUser } = useUserContext()
    const [editingAssignedAthlete, setEditingAssignedAthlete] = useState<boolean>(false)
    const [assignedAthlete, setAssignedAthlete] = useState<string>("")
    const [assignAthleteError, setAssignAthleteError] = useState<string>("")

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
            fetchData()
                .finally(() => {
                    setLoading(false)
                })
        }
    }, [router.isReady])

    useEffect(() => {
        if (!loading) {
            setAssignedAthlete(program!.athleteEmail)
        }
    }, [program, loading])

    const handleAthleteChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setAssignedAthlete(e.target.value)
    }

    const saveAthleteAssignment = async () => {
        try {
            const updatedProgram = await Api.assignProgram(program!.id, assignedAthlete)
            setProgram(updatedProgram)
            setEditingAssignedAthlete(false)
            setAssignAthleteError("")
        } catch (error: any) {
            if (error instanceof NeedLogin) {
                window.location.href = "/login"
            } else if (error.message) {
                if (error.message.includes(assignedAthlete)) {
                    setAssignAthleteError("Invalid athlete email. Please check for typos")
                } else {
                    setAssignAthleteError("Unexpected error. Please try again later")
                }
            } else {
                setAssignAthleteError("Unexpected error. Please try again later")
            }
        }
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
                        {
                            user!.userType == UserType.COACH ?
                                <div className="flex w-fit">
                                    <label>Assigned Athlete: </label>
                                    {
                                        editingAssignedAthlete ?
                                            <input id="assigned-athlete" className="pl-1 border-black border rounded-md ml-1" type="email" value={assignedAthlete} onChange={handleAthleteChange} placeholder="Athlete email" />
                                            :
                                            <h3 className="ml-1">{assignedAthlete || "None"}</h3>
                                    }
                                    {
                                        editingAssignedAthlete ?
                                            <div className="flex">
                                                <svg onClick={() => setEditingAssignedAthlete(false)} xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className="w-6 h-6 hover:cursor-pointer mr-0.5">
                                                    <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
                                                </svg>
                                                <svg onClick={saveAthleteAssignment} xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className="w-6 h-6 hover:cursor-pointer">
                                                    <path strokeLinecap="round" strokeLinejoin="round" d="M4.5 12.75l6 6 9-13.5" />
                                                </svg>
                                            </div>
                                            :
                                            <svg onClick={() => setEditingAssignedAthlete(true)} xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className="w-6 h-6 pt-0.5 ml-2 hover:cursor-pointer">
                                                <path strokeLinecap="round" strokeLinejoin="round" d="M16.862 4.487l1.687-1.688a1.875 1.875 0 112.652 2.652L10.582 16.07a4.5 4.5 0 01-1.897 1.13L6 18l.8-2.685a4.5 4.5 0 011.13-1.897l8.932-8.931zm0 0L19.5 7.125M18 14v4.75A2.25 2.25 0 0115.75 21H5.25A2.25 2.25 0 013 18.75V8.25A2.25 2.25 0 015.25 6H10" />
                                            </svg>
                                    }
                                </div>
                                :
                                <h2 className="text-lg">
                                    Written by: {program.coachEmail}
                                </h2>
                        }
                        {
                            assignAthleteError.length > 0 &&
                            <p className="text-red-500">{assignAthleteError}</p>
                        }
                    </div>
                    <div className="flex flex-col items-center mt-8">
                        <WeekList weeks={program.weeks} setProgram={setProgram} userType={user!.userType} programId={program.id} setPageError={setError} loading={loading} />
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

