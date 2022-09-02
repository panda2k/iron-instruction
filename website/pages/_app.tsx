import '../styles/globals.css'
import '../styles/styles.css'
import type { AppProps } from 'next/app'
import { UserContextProvider } from '../context/UserContext'

function MyApp({ Component, pageProps }: AppProps) {
    return (
        <UserContextProvider>
            <Component {...pageProps} />
        </UserContextProvider>
    )
}

export default MyApp
