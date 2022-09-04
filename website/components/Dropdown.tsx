import { NextPage } from "next"
import { useUserContext } from "../context/UserContext"
import Api from '../utils/api'

type Props = {
    heading: string,
    menuItems: Array<{ name: string, href: string }>,
    logoutButton: boolean
}

const Dropdown: NextPage<Props> = (props: Props) => {
    const { user, setUser } = useUserContext()

    const handleClick = () => {
        const dropdown = document.querySelector(`[aria-labelledby='menu-button']`) as HTMLElement
        if (dropdown.style.display == "none" || dropdown.style.display.length == 0) {
            dropdown.style.display = "block"
        } else {
            dropdown.style.display = "none"
        }
    }

    const handleLogout = async (e: React.FormEvent) => {
        e.preventDefault()
        window.location.href = "/"
        await Api.logout()
        setUser(null)
    }

    return (
        <div className="relative inline-block text-left">
            <div>
                <button onClick={handleClick} type="button" className="inline-flex w-full justify-center rounded-md border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 shadow-sm hover:bg-gray-50" id="menu-button" aria-expanded="true" aria-haspopup="true">
                    {props.heading}
                    <svg className="-mr-1 ml-2 h-5 w-5" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor" aria-hidden="true">
                        <path fillRule="evenodd" d="M5.23 7.21a.75.75 0 011.06.02L10 11.168l3.71-3.938a.75.75 0 111.08 1.04l-4.25 4.5a.75.75 0 01-1.08 0l-4.25-4.5a.75.75 0 01.02-1.06z" clipRule="evenodd" />
                    </svg>
                </button>
            </div>
            <div className="hidden divide-y divide-gray-200 absolute right-0 z-10 mt-2 w-40 origin-top-right rounded-md bg-white shadow-lg ring-1 ring-black ring-opacity-5 focus:outline-none" role="menu" aria-orientation="vertical" aria-labelledby="menu-button" tabIndex={-1}>
                <div className="py-1" role="none">
                    {props.menuItems.map(item => {
                        return (
                            <a href={item.href} className="text-gray-700 block px-4 py-2 text-sm" role="menuitem" tabIndex={-1} >{item.name}</a>
                        )
                    })}
                </div>
                {props.logoutButton && (
                    <div className="py-1">
                        <form onSubmit={handleLogout}>
                            <button type="submit" className="text-gray-700 block w-full px-4 py-2 text-left text-sm" role="menuitem" tabIndex={-1}>Sign out</button>
                        </form>
                    </div>
                )}
            </div>
        </div >
    )
}

export default Dropdown

