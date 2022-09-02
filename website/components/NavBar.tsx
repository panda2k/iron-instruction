import { NextPage } from "next";
import Link from "next/link";
import { useUserContext } from "../context/UserContext";
import Dropdown from "./Dropdown";
import Logo from "./Logo";

type Props = {
    loading: boolean
}

const NavBar: NextPage<Props> = (props: Props) => {
    const { user, setUser } = useUserContext()

    return (
        <div className="flex justify-between ml-8 mt-6 mr-8">
            <Logo />
            {!props.loading && (
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
                        <Dropdown logoutButton={true} heading={`${user.name}`} menuItems={[{ name: "Profile", href: "/profile" }, { name: "Programs", href: "/programs" }]} />
                    </div>
            )
            }
        </div>
    )
}

export default NavBar

