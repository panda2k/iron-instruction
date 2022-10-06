import { NextPage } from "next";
import { useEffect, useState } from "react";
import Accordion from "../components/Accordion";
import CreateProgram from "../components/CreateProgram";
import Modal from "../components/Modal";
import NavBar from "../components/NavBar";
import ProgramQuickview from "../components/ProgramQuickview";
import { useUserContext } from "../context/UserContext";
import Api from '../utils/api'
import { NeedLogin } from "../utils/api.errors";
import { Athlete, Program, User, UserType } from "../utils/api.types";

const Dashboard: NextPage = () => {
    const [loading, setLoading] = useState<boolean>(true)
    const { user, setUser } = useUserContext()
    const [userForm, setUserForm] = useState<User>(user as User)
    const [editing, setEditing] = useState<boolean>(false)
    const [programs, setPrograms] = useState<Program[]>([])
    const [modalOpen, setModalOpen] = useState<boolean>(false)
    const [error, setError] = useState<string>("")

    useEffect(() => {
        const fetchData = async () => {
            try {
                const fetchedUser: User = await Api.getUserInfo()
                setUser(fetchedUser)
                setUserForm(fetchedUser)

                setPrograms(await Api.getUserPrograms())
            } catch (error) {
                window.location.href = "/login"
            }
            setLoading(false)
        }

        fetchData()
    }, [])

    // load new programs after potentially creating a new one
    useEffect(() => {
        const fetchData = async () => {
            try {
                setPrograms(await Api.getUserPrograms())
            } catch (error) {
                window.location.href = "/login"
            }
        }
        if (!loading) {
            fetchData()
        }
    }, [modalOpen])

    // style form
    useEffect(() => {
        if (!loading) {
            const targetWidth: number = (document.querySelector(user!.userType == UserType.ATHLETE ? "#reference" : "#profile-form div") as HTMLDivElement).offsetWidth

            document.querySelectorAll(".whitespace-nowrap:not(#reference)").forEach((element) => {
                const inputWidth: number = targetWidth - element.querySelector("label")!.offsetWidth
                const input = element.querySelector("input") as HTMLElement
                input.style.width = `${inputWidth}px`
            })
        }
    }, [loading, editing])

    const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
        setUserForm({ ...userForm, [e.target.name]: e.target.value })
    }

    const toggleEdit = () => {
        setEditing(!editing)
    }

    const cancelEdit = () => {
        toggleEdit()
        setUserForm(user as User)
        if (user!.userType == UserType.ATHLETE) {
            if ((user as Athlete).dob == null) {
                (document.querySelector("#dob") as HTMLInputElement).value = ""
            }

            if (!(user as Athlete).weightClass) {
                (document.querySelector("#weightClass") as HTMLSelectElement).selectedIndex = 0
            }
        }
    }

    const saveProfile = async () => {
        let updatedUser: User = await Api.updateUser(userForm.email, userForm.name)
            .catch(error => {
                if (error instanceof NeedLogin) {
                    window.location.href = "/login"
                }
                setError("Unexpected error")
                return Promise.reject("Unexpected error")
            })

        if (user!.userType == UserType.ATHLETE) {
            const castedForm: Athlete = userForm as Athlete
            updatedUser = await Api.updateAthleteInfo(castedForm.weightClass, castedForm.weight, castedForm.dob, castedForm.squatMax, castedForm.benchMax, castedForm.deadliftMax, castedForm.height)
                .catch(error => {
                    if (error instanceof NeedLogin) {
                        window.location.href = "/login"
                    }
                    setError("Unexpected error")
                    return Promise.reject("Unexpected error")
                })
        }

        setUser(updatedUser)
        setUserForm(updatedUser)
        toggleEdit()
    }

    const generateAccordionContent = (): { heading: string, body: React.ReactElement }[] => {
        return programs.map((program) => {
            return {
                heading: program.name,
                body: <ProgramQuickview userType={user!.userType} program={program} />
            }
        })
    }

    return (
        <div className="w-100 flex flex-col">
            <NavBar loading={loading} />
            {
                !loading && (
                    <div className="flex flex-row items-start justify-evenly">
                        <div className="outline-2 rounded-md mt-8 ring-black ring-1 ring-opacity-30 py-3 px-5">
                            <h1 className="underline text-center text-3xl mb-3">Profile</h1>
                            <form id="profile-form">
                                <div className="whitespace-nowrap w-fit">
                                    <label htmlFor="name">Name: </label>
                                    <input onChange={handleChange} className="enabled:border-black enabled:pl-1 enabled:mb-1.5 enabled:border enabled:rounded-md focus:outline-none" type="text" name="name" value={userForm.name} disabled={!editing} id="name" />
                                </div>
                                <div className="whitespace-nowrap w-fit">
                                    <label htmlFor="email">Email: </label>
                                    <input onChange={handleChange} className="enabled:border-black enabled:pl-1  enabled:mb-1.5 enabled:border enabled:rounded-md focus:outline-none" type="email" name="email" value={userForm.email} disabled={!editing} id="email" />
                                </div>
                                {
                                    userForm!.userType == UserType.ATHLETE &&
                                    (
                                        <div>
                                            <div className="w-fit whitespace-nowrap">
                                                <label htmlFor="dob">Date of Birth: </label>
                                                <input className="enabled:border-black enabled:pl-1 enabled:mb-1.5 enabled:border enabled:rounded-md focus:outline-none" onChange={handleChange} required={true} type="date" name="dob" value={(userForm as Athlete).dob} disabled={!editing} id="dob" />
                                            </div>
                                            <div className="w-fit whitespace-nowrap">
                                                <label htmlFor="weight">Weight (kg): </label>
                                                <input className="enabled:border-black enabled:pl-1 enabled:mb-1.5 enabled:border enabled:rounded-md focus:outline-none" onChange={handleChange} type="number" name="weight" value={(userForm as Athlete).weight} disabled={!editing} id="weight" />
                                            </div>
                                            <div className="w-fit whitespace-nowrap" id="reference">
                                                <label htmlFor="weightClass">Weight Class: </label>
                                                <select className="enabled:border-black enabled:pl-1 enabled:mb-1.5 enabled:border enabled:py-0.5 enabled:rounded-md focus:outline-none" onChange={handleChange} name="weightClass" id="weightClass" value={(userForm as Athlete).weightClass} disabled={!editing}>
                                                    <option disabled={true} selected={true}>No weight class saved</option>
                                                    <option value={"44kg / 97lbs"}>44kg / 97lbs</option>
                                                    <option value={"48kg / 105.7lbs"}>48kg / 105.7lbs</option>
                                                    <option value={"52kg / 114.5lbs"}>52kg/ 114.5lbs</option>
                                                    <option value={"56kg / 123.5lbs"}>56kg / 123.5lbs</option>
                                                    <option value={"60kg / 132.2lbs"}>60kg / 132.2lbs</option>
                                                    <option value={"67.5kg / 148.7lbs"}>67.5kg/ 148.7lbs</option>
                                                    <option value={"75kg / 165.2lbs"}>75kg / 165.2lbs</option>
                                                    <option value={"82.5kg / 181.7lbs"}>82.5kg / 181.7lbs</option>
                                                    <option value={"90kg / 198.2lbs"}>90kg / 198.2lbs</option>
                                                    <option value={"100kg / 220lbs"}>100kg / 220lbs</option>
                                                    <option value={"100+kg / 220+lbs"}>100+kg / 220+lbs</option>
                                                    <option value={"110kg / 242lbs"}>110kg / 242lbs</option>
                                                    <option value={"125kg / 275lbs"}>125kg / 275lbs</option>
                                                    <option value={"140kg / 308.5lbs"}>140kg / 308.5lbs</option>
                                                    <option value={"140+kg / 308.5+lbs"}>140+kg / 308.5+lbs</option>
                                                </select>
                                            </div>
                                            <div className="w-fit whitespace-nowrap">
                                                <label htmlFor="weight">Height (in): </label>
                                                <input className="enabled:border-black enabled:pl-1 enabled:mb-1.5 enabled:border enabled:rounded-md focus:outline-none" onChange={handleChange} type="number" name="height" value={(userForm as Athlete).height} disabled={!editing} id="height" />
                                            </div>
                                            <div className="whitespace-nowrap w-fit">
                                                <label htmlFor="squatMax">Squat Max (kg): </label>
                                                <input className="enabled:border-black enabled:pl-1 enabled:mb-1.5 enabled:border enabled:rounded-md focus:outline-none" onChange={handleChange} type="number" name="squatMax" value={(userForm as Athlete).squatMax} disabled={!editing} id="squatMax" />
                                            </div>
                                            <div className="whitespace-nowrap w-fit">
                                                <label htmlFor="benchMax">Bench Max (kg): </label>
                                                <input className="enabled:border-black enabled:pl-1 enabled:mb-1.5 enabled:border enabled:rounded-md focus:outline-none" onChange={handleChange} type="number" name="benchMax" value={(userForm as Athlete).benchMax} disabled={!editing} id="benchMax" />
                                            </div>
                                            <div className="whitespace-nowrap w-fit">
                                                <label htmlFor="deadliftMax">Deadlift Max (kg): </label>
                                                <input className="enabled:border-black enabled:pl-1 enabled:mb-1.5 enabled:border enabled:rounded-md focus:outline-none" onChange={handleChange} type="number" name="deadliftMax" value={(userForm as Athlete).deadliftMax} disabled={!editing} id="deadliftMax" />
                                            </div>
                                        </div>
                                    )
                                }
                                {editing ?
                                    <div className="flex flex-row justify-center mt-3 w-full">
                                        <button className="mr-2 purple-bg rounded-md px-6 py-1 text-white" type="button" onClick={cancelEdit}>Cancel</button>
                                        <button className="purple-bg rounded-md px-6 py-1 text-white" type="button" onClick={saveProfile}>Save</button>

                                    </div>
                                    :
                                    <div className="flex flex-row justify-center mt-3 w-full">
                                        <button className="purple-bg rounded-md px-6 py-1 text-white" type="button" onClick={toggleEdit}>Edit</button>
                                    </div>
                                }
                                {error.length > 0 && <p className="text-center break-words min-w-full w-0 text-red-600 mt-2"> {error} </p>}
                            </form>

                        </div>
                        <div className="flex flex-col items-center outline-2 rounded-md mt-8 ml-10 ring-black ring-1 ring-opacity-30 py-3 px-5">
                            <h1 className="border-b-black border-b-2 pb-0.5 text-3xl w-fit mb-3">
                                Programs
                            </h1>
                            {
                                programs.length == 0 ?
                                    <h2>{user!.userType == UserType.ATHLETE ? "No programs assigned to you" : "No programs created"}</h2>
                                    :
                                    <div>
                                        {
                                            <Accordion animationTime={200} items={generateAccordionContent()} loaded={loading} />
                                        }
                                    </div>
                            }
                            {user!.userType == UserType.COACH &&
                                <div className="w-full border-t-black border-opacity-40 border-t">
                                    <button onClick={() => setModalOpen(true)} type="button" className="purple-bg rounded-md px-6 py-1 text-white w-full mt-4">Create Program</button>
                                    <Modal open={modalOpen} setOpen={setModalOpen}>
                                        <CreateProgram cancelHandler={() => setModalOpen(false)}></CreateProgram>
                                    </Modal>
                                </div>
                            }
                        </div>
                    </div>
                )
            }
        </div >
    )
}

export default Dashboard

