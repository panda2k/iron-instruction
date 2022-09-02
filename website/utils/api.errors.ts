export class NeedLogin extends Error {
    constructor(message: string) {
        super(message)

        Object.setPrototypeOf(this, NeedLogin.prototype)
    }
}
