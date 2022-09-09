import { NextPage } from "next"
import Link from "next/link"
import { Program } from "../utils/api.types"
import Accordion from "./Accordion"
import Dropdown from "./Dropdown"

type Props = {
    program: Program
}

const ProgramQuickview: NextPage<Props> = (props: Props) => {
    const generateWeekContent = () => {
        return (
            <div>
                {
                    props.program.weeks.map(week => {
                        return (
                            <div></div>
                        )
                    }
                    )}

            </div>
        )
    }

    return (
        <div className="flex flex-col">
            <p>
                Description: {props.program.description}
            </p>
            <p>
                Athlete: {props.program.athleteEmail || "No athlete assigned"}
            </p>
            {
                props.program.weeks.length == 0 ?
                    <p>Weeks: No content yet</p>
                    :
                    <Accordion items={[{ heading: "Weeks", body: generateWeekContent() }]} animationTime={200} />
            }
            <div className="flex flex-row justify-center mt-3">
                <Link href={`/program/${props.program.id}`}>
                    <button type="button" className="w-full purple-bg rounded-md px-6 py-1 text-white">Edit</button>
                </Link>
            </div>
        </div>
    )
}

export default ProgramQuickview

