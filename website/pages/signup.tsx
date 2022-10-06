import { AxiosError } from "axios";
import { NextPage } from "next";
import Head from "next/head";
import Link from "next/link";
import { useState } from "react";
import Logo from "../components/Logo";
import { useUserContext } from "../context/UserContext";
import Api from '../utils/api'
import { ErrorResponse, User, UserType } from "../utils/api.types";

const SignUp: NextPage = () => {
    const [input, setInput] = useState({
        email: '',
        password: '',
        name: '',
        accountType: 'COACH'
    })

    const { user, setUser } = useUserContext()
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState("")

    const inputHandler = (e: React.ChangeEvent<HTMLInputElement>) => {
        setInput({ ...input, [e.target.name]: e.target.value })
    }

    const submitSignup = async (e: React.FormEvent) => {
        e.preventDefault()
        setLoading(true)
        try {
            await Api.createUser(input.name, input.email, input.password, UserType[input.accountType as keyof typeof UserType])
            await Api.login(input.email, input.password)
            const userInfo: User = await Api.getUserInfo()
            setUser(userInfo)
            window.location.href = "/"
        } catch (error) {
            if (error instanceof AxiosError && error.response) {
                if (error.response.data) {
                    const errorMessage: string = (error.response.data as unknown as ErrorResponse).message
                    if (errorMessage.includes("already exists")) {
                        setError(`Account with email ${input.email} already exists`)
                    } else {
                        setError("Unexpected error. Please try again")
                    }
                } else {
                    setError("Unexpected error. Please try again")
                }
            } else {
                setError("Unexpected error. Please try again")
            }
        }

        setLoading(false)
    }

    return (
        <div className="flex-col justify-center items-center h-full">
            <Head>
                <title>Sign Up</title>
            </Head>
            <div className="h-full flex items-center justify-center flex-col w-fit m-auto">
                <form className="w-fit flex rounded-md flex-col justify-center items-center border border-black pt-12 pb-8 px-8" onSubmit={submitSignup}>
                    <Logo />
                    <div className="w-full toggle mt-8" onChange={inputHandler}>
                        <input disabled={loading} type="radio" name="accountType" value="COACH" id="coachToggle" defaultChecked />
                        <label htmlFor="coachToggle">Coach</label>
                        <input disabled={loading} type="radio" name="accountType" value="ATHLETE" id="athleteToggle" />
                        <label htmlFor="athleteToggle">Athlete</label>
                    </div>
                    <input disabled={loading} className="mt-3 w-fit border-black border rounded-md py-0.5 px-2" type="text" name="name" onChange={inputHandler} placeholder="Name" value={input.name} />
                    <input disabled={loading} className="w-fit border-black border rounded-md py-0.5 px-2 mt-3" type="email" name="email" onChange={inputHandler} placeholder="Email" value={input.email} />
                    <input disabled={loading} className="w-fit border-black border rounded-md py-0.5 px-2 mt-3" type="password" name="password" onChange={inputHandler} placeholder="Password" value={input.password} />
                    <input type="submit" disabled={loading} className="disabled:opacity-80 disabled:cursor-progress purple-bg mt-6 cursor-pointer py-1 px-6 rounded-md text-white" value="Sign Up" />
                    {error.length != 0 && <p className="text-center break-words min-w-full w-0 text-red-600 mt-2"> {error} </p>}
                </form>
                <div className="border underline border-black rounded-md w-full mt-5 text-center py-3 cursor-pointer">
                    <Link href="/login"><div>Already have an account? <br /> Log in instead.</div></Link>
                </div>
            </div>
        </div>
    )
}

export default SignUp

