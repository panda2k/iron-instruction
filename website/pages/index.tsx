import type { NextPage } from 'next'
import Head from 'next/head'
import Link from 'next/link'
import { useEffect, useState } from 'react'
import NavBar from '../components/NavBar'
import { useUserContext } from '../context/UserContext'
import Api from '../utils/api'
import { User } from '../utils/api.types'

const Home: NextPage = () => {
    const [loading, setLoading] = useState(true)
    const { user, setUser } = useUserContext()

    useEffect(() => {
        setLoading(true)
        const getUser = async () => {
            if (user == null) {
                const user: User | null = await Api.getUserInfo()
                    .catch(() => {
                        return null
                    })
                setUser(user)
            }
            setLoading(false)
        }
        getUser()
    }, [])

    return (
        <div className="flex flex-col h-full w-full">
            <Head>
                <title>Iron Instruction</title>
            </Head>
            <NavBar loading={loading} />
            <div className="flex flex-col h-full justify-center align-middle">
                <h1 className='text-8xl mb-5 font-extrabold text-center'>Strength Coaching <br />Made Simple</h1>
                <h2 className='text-2xl text-gray-500 text-center'>It&#39;s time to ditch the Excel sheets and create intuitive programs without the boilerplate.</h2>
                <div className="flex justify-center w-full mt-8">
                    <Link href="/signup">
                        <button className="purple-bg text-lg py-2 px-16 rounded-md text-white w-fit">Get Started</button>
                    </Link>
                </div>
            </div>
        </div >
    )
}

export default Home

