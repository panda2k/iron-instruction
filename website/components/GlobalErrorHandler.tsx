import { Component, ReactNode } from "react";
import { NeedLogin } from "../utils/api.errors";

type Props = {
    children?: ReactNode
}

type State = {
    hasError: boolean
}

class GlobalErrorHandler extends Component<Props, State> {
    constructor(props: any) {
        super(props)
        this.state = { hasError: false }
    }

    componentDidCatch(error: Error) {
        if (error instanceof NeedLogin) {
            window.location.href = "/login"
        } else {
            this.setState({ hasError: true })
            console.log("Uncaught error!\n" + error)
        }
    }

    render() {
        if (this.state.hasError) {
            return <h1>Something unexpected happened</h1>
        } else {
            return this.props.children
        }
    }
}

export default GlobalErrorHandler

