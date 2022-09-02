import '../styles/globals.css'
import '../styles/styles.css'
import type { AppProps } from 'next/app'
import { UserContextProvider } from '../context/UserContext'
import GlobalErrorHandler from '../components/GlobalErrorHandler'

function MyApp({ Component, pageProps }: AppProps) {
    return (
        <GlobalErrorHandler>
            <UserContextProvider>
                <Component {...pageProps} />
            </UserContextProvider>
        </GlobalErrorHandler>
    )
}

export default MyApp
