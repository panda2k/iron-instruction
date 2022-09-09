import { AxiosError } from "axios";
import { NextPage } from "next";
import { useState } from "react";
import Api from '../utils/api'
import { NeedLogin } from "../utils/api.errors";

type CreateProgramForm = {
    name: string,
    description: string
}

type Props = {
    cancelHandler: () => void
}

const CreateProgram: NextPage<Props> = (props) => {
    const [createProgramForm, setCreateProgramForm] = useState<CreateProgramForm>({ name: "", description: "" })
    const [saving, setSaving] = useState<boolean>(false)
    const [error, setError] = useState<string>("")

    const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
        setCreateProgramForm({ ...createProgramForm, [e.target.name]: e.target.value })
    }

    const createProgram = async(e: React.FormEvent) => {
        e.preventDefault()
        setSaving(true) 
        try {
            await Api.createProgram(createProgramForm.name, createProgramForm.description)
            props.cancelHandler()
        } catch (error) {
            if (error instanceof AxiosError) {
                if (error.response!.data) {
                    console.log(error.response!.data)
                    if (error.response!.data.message.toLowerCase().includes("empty")) {
                        setError("Make sure to fill out the name field") 
                    } else {
                        setError("Unexpected error. Please try again")
                    }
                }
            } else if (error instanceof NeedLogin) {
                window.location.href = "/login"
                setError("Unexpected error. Please try again")
            }
        }
        setSaving(false)
    }

    return (
        <div className="flex flex-col items-center">
            <h1 className="text-xl border-b-black border-b mb-3">Create Program</h1>
            <form className="w-full" onSubmit={createProgram}>
                <input name="name" required={true} className="pl-2 border-black border rounded-md py-0.5 w-full" type="text" onChange={handleChange} placeholder="Program Name"/>
                <textarea name="description" className="pl-2 bg-white border-black border rounded-md mt-3 w-full resize-none h-32" onChange={handleChange} placeholder="Program Description"></textarea>
                <div className="flex justify-center mt-4">
                    <input disabled={saving} onClick={props.cancelHandler} className="mr-2 purple-bg rounded-md px-6 py-1 text-white disabled:opacity-80 hover:cursor-pointer disabled:cursor-progress" type="button" value="Cancel"/>
                    <input disabled={saving} type="submit" value={saving ? "Saving" : "Save"} className="disabled:opacity-80 disabled:cursor-progress hover:cursor-pointer purple-bg rounded-md px-6 py-1 text-white"/>
                </div>
                {error.length > 0 && <p className="text-center break-words min-w-full w-0 text-red-600 mt-2"> {error} </p>}
            </form>
        </div>
    )
}

export default CreateProgram

