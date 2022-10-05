export class NeedLogin extends Error {
    constructor(message: string) {
        super(message)

        Object.setPrototypeOf(this, NeedLogin.prototype)
    }
}

export class ApiError extends Error {
    errorCode: number
    constructor(message: string, errorCode: number) {
        super(message)
        Object.setPrototypeOf(this, ApiError.prototype);
        this.errorCode = errorCode
    }
}

