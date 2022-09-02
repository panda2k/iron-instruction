import { createContext, ReactNode, useContext, useState } from 'react'
import { User } from '../utils/api.types'

type UserContextType = {
    user: User | null,
    setUser: (user: User | null) => void
}

const userContextDefault: UserContextType = {
    user: null,
    setUser: (user: User | null) => { }
}

const UserContext = createContext<UserContextType>(userContextDefault)

export const useUserContext = () => useContext(UserContext)

type Props = {
    children: ReactNode
}

export const UserContextProvider = ({ children }: Props) => {
    const [user, setUser] = useState<User | null>(null)

    return (
        <UserContext.Provider value={{ user, setUser }}>
            {children}
        </UserContext.Provider>
    )
}

