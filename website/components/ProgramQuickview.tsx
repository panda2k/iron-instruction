import { NextPage } from "next"
import Link from "next/link"
import { Program, UserType } from "../utils/api.types"

type Props = {
    program: Program
    userType: UserType
}

const ProgramQuickview: NextPage<Props> = (props: Props) => {
    return (
        <div className="flex flex-col">
            <p>
                Description: {props.program.description}
            </p>
            <p>
                {props.userType == UserType.COACH ?
                    `Athlete: ${props.program.athleteEmail || "No athlete assigned"}`
                    :
                    `Coach: ${props.program.coachEmail}`
                }
            </p>
            <p>Total Weeks: {props.program.weeks.length}</p>
            <div className="flex flex-row justify-center mt-3">
                <Link href={`/program/${props.program.id}`}>
                    <button type="button" className="w-full purple-bg rounded-md px-6 py-1 text-white">
                        {props.userType == UserType.COACH ? "Edit" : "View"}
                    </button>
                </Link>
            </div>
        </div>
    )
}

export default ProgramQuickview

