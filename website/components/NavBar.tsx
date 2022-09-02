import { NextPage } from "next";
import Link from "next/link";
import { useUserContext } from "../context/UserContext";
import Logo from "./Logo";

const NavBar: NextPage = () => {
    const { user, setUser } = useUserContext()

    return (
        <div className="flex justify-between ml-8 mt-6 mr-8">
            <Logo />
            {
                user == null ?
                    <div>
                        <Link href="/login">
                            <button className="mr-2 purple-bg py-1 px-8 rounded-md text-white">Login</button>
                        </Link>
                        <Link href="/signup">
                            <button className="purple-bg py-1 px-6 rounded-md text-white">Sign Up</button>
                        </Link>
                    </div>
                    :
                    <div>
                        Welcome back {user.name}
                    </div>
            }
        </div>
    )
}

export default NavBar

