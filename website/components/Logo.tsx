import { NextPage } from "next";
import Link from "next/link";

const Logo: NextPage = () => {
    return (
        <Link href="/">
            <a>
                <div className="flex flex-col text-center font-bold text-xl w-fit">
                    <h4 className="-mb-3">Iron</h4>
                    <h4 className="w-fit">Instruction</h4>
                </div>
            </a>
        </Link>
    )
}

export default Logo

