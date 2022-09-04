import { AxiosError } from "axios";
import { NextPage } from "next";
import Link from "next/link";
import { useState } from "react";
import Logo from "../components/Logo";
import { useUserContext } from "../context/UserContext";
import Api from '../utils/api'
import { ErrorResponse, User } from "../utils/api.types";

const Login: NextPage = () => {
    const [input, setInput] = useState({
        email: '',
        password: ''
    })

    const { user, setUser } = useUserContext()
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState("")

    const inputHandler = (e: React.ChangeEvent<HTMLInputElement>) => {
        setInput({ ...input, [e.target.name]: e.target.value })
    }

    const submitLogin = async (e: React.FormEvent) => {
        e.preventDefault()
        setLoading(true)
        try {
            await Api.login(input.email, input.password)
            const userInfo: User = await Api.getUserInfo()
            setUser(userInfo)
            window.location.href = "/"
        } catch (error) {
            if (error instanceof AxiosError && error.response) {
                if (error.response.data) {
                    const errorMessage: string = (error.response.data as unknown as ErrorResponse).message
                    if (errorMessage.includes("Incorrect password")) {
                        setError("Incorrect password")
                    } else if (errorMessage.includes("No user found")) {
                        setError(`No user found with the email ${input.email}`)
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
            <div className="h-full flex items-center justify-center flex-col w-fit m-auto">
                <form className="flex rounded-md flex-col justify-center items-center border border-black pt-12 pb-8 px-8" onSubmit={submitLogin}>
                    <Logo />
                    <input className="mt-8 w-fit border-black border rounded-md py-0.5 px-2" type="email" name="email" onChange={inputHandler} placeholder="Email" value={input.email} />
                    <input className="w-fit border-black border rounded-md py-0.5 px-2 mt-3" type="password" name="password" onChange={inputHandler} placeholder="Password" value={input.password} />
                    <input type="submit" disabled={loading} className="disabled:opacity-80 disabled:cursor-progress purple-bg mt-6 cursor-pointer py-1 px-6 rounded-md text-white" value="Login" />
                    {error.length != 0 && <p className="text-center break-words min-w-full w-0 text-red-600 mt-2"> {error} </p>}
                </form>
                <div className="border underline cursor-pointer border-black rounded-md w-full mt-5 text-center py-3">
                    <Link href="/signup">No account? Sign up instead.</Link>
                </div>
            </div>
        </div>
    )
}

export default Login

