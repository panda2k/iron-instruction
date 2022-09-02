import axios, { AxiosError, AxiosInstance, AxiosRequestConfig, AxiosResponse } from "axios";
import { NeedLogin } from "./api.errors";
import { ErrorResponse, Program, User, UserType } from "./api.types";

class Api {
    private client: AxiosInstance

    constructor() {
        this.client = axios.create({
            baseURL: "http://localhost:8080/api/v1",
            responseType: "json",
            withCredentials: true
        })
        this.client.interceptors.response.use((response: AxiosResponse) => {
            return response
        }, async (error: AxiosError) => {
            const originalRequest: AxiosRequestConfig = error.config
            const errorMessage: ErrorResponse = (error.response?.data) as unknown as ErrorResponse
            if (error.response?.status == 403 && errorMessage.message.toLowerCase().includes("expired")) {
                await this.getNewTokens()
                return this.client(originalRequest)
            } else if (originalRequest.url?.match("refreshtoken")) { // failed to refresh token
                throw new NeedLogin("Need new login")
            } else {
                return Promise.reject(error)
            }
        })
    }

    public async logout(): Promise<AxiosResponse> {
        return this.client.post("/logout")
    }

    public async getNewTokens(): Promise<AxiosResponse> {
        return this.client.post("/refreshtoken")
    }

    public async getUserInfo(): Promise<User> {
        return (await this.client.get("/users/me")).data as unknown as User
    }

    public async createUser(name: string, email: string, password: string, userType: UserType): Promise<User> {
        return (await this.client.post("/users", {
            name: name,
            email: email,
            password: password,
            userType: userType
        }, { withCredentials: false })).data as unknown as User
    }

    /**
    * @param {string} dob - date of birth in yyyy-MM-dd format
    */
    public async updateAthleteInfo(weightClass: string, weight: string, dob: string, squatMax: number, benchMax: number, deadliftMax: number, height: number): Promise<User> {
        return (await this.client.post("/users/me", {
            weightClass: weightClass,
            weight: weight,
            dob: dob,
            squatMax: squatMax,
            benchMax: benchMax,
            deadliftMax: deadliftMax,
            height: height
        })).data as unknown as User
    }

    public async login(email: string, password: string): Promise<AxiosResponse> {
        return this.client.post("/login", {
            email: email,
            password: password
        })
    }

    public async getUserPrograms(): Promise<Program[]> {
        return (await this.client.get(`/programs/user/me`)).data as unknown as Program[]
    }
}

export default new Api()

