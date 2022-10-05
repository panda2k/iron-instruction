import axios, { AxiosError, AxiosInstance, AxiosRequestConfig, AxiosResponse } from "axios";
import { ApiError, NeedLogin } from "./api.errors";
import { ErrorResponse, Exercise, PercentageOptions, Program, User, UserType } from "./api.types";

class Api {
    private client: AxiosInstance

    constructor() {
        this.client = axios.create({
            baseURL: "http://localhost:8080/api/v1",
            responseType: "json",
            withCredentials: true
        })
        this.client.interceptors.response.use((response: AxiosResponse) => {
            if (response.data.userType) {
                response.data.userType = UserType[response.data.userType as keyof typeof UserType]
            }

            if (response.data.dob) {
                response.data.dob = response.data.dob.split("T")[0]
            }

            return response
        }, async (error: AxiosError) => {
            const originalRequest: AxiosRequestConfig = error.config
            const errorMessage: ErrorResponse = (error.response?.data) as unknown as ErrorResponse
            if (error.response?.status == 403 && errorMessage.message.toLowerCase().includes("expired")) {
                await this.getNewTokens()
                return this.client(originalRequest)
            } else if (errorMessage.message.includes("Invalid token")) {
                throw new NeedLogin("Need new login")
            } else if (originalRequest.url?.match("refreshtoken")) { // failed to refresh token
                throw new NeedLogin("Need new login")
            } else if (errorMessage) {
                throw new ApiError(errorMessage.message, error.response!.status)
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

    public async updateUser(email: string, name: string): Promise<User> {
        return (await this.client.post("/users/me", {
            email: email,
            name: name
        })).data as unknown as User
    }

    public async createUser(name: string, email: string, password: string, userType: UserType): Promise<User> {
        return (await this.client.post("/users", {
            name: name,
            email: email,
            password: password,
            userType: userType.toString().toUpperCase()
        }, { withCredentials: false })).data as unknown as User
    }

    /**
    * @param {string} dob - date of birth in yyyy-MM-dd format
    */
    public async updateAthleteInfo(weightClass: string, weight: number, dob: string, squatMax: number, benchMax: number, deadliftMax: number, height: number): Promise<User> {
        return (await this.client.post("/users/me/athlete", {
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

    public async getProgram(programId: string): Promise<Program> {
        return (await this.client.get(`/programs/${programId}`)).data as unknown as Program
    }

    public async assignProgram(programId: string, athleteEmail: string): Promise<Program> {
        return (await this.client.post(`programs/${programId}/assign`, {
            email: athleteEmail
        })).data as unknown as Program
    }

    public async createWeek(programId: string, notes: string): Promise<Program> {
        return (await this.client.post(`/programs/${programId}/weeks`, {
            note: notes
        })).data as unknown as Program
    }

    public async deleteWeek(programId: string, weekId: string): Promise<Program> {
        return (await this.client.delete(
            `/programs/${programId}/weeks/${weekId}`
        )).data as unknown as Program
    }

    public async updateWeekCoachNote(programId: string, weekId: string, notes: string): Promise<Program> {
        return (await this.client.post(`programs/${programId}/weeks/${weekId}/notes`, {
            note: notes
        })).data as unknown as Program
    }

    public async updateWeekAthleteNote(programId: string, weekId: string, notes: string): Promise<Program> {
        return (await this.client.patch(`programs/${programId}/weeks/${weekId}/notes`, {
            note: notes
        })).data as unknown as Program
    }

    public async createDay(programId: string, weekId: string, coachNotes: string): Promise<Program> {
        return (await this.client.post(`programs/${programId}/weeks/${weekId}/days`, {
            note: coachNotes
        }
        )).data as unknown as Program
    }

    public async deleteDay(programId: string, weekId: string, dayId: string): Promise<Program> {
        return (await this.client.delete(
            `programs/${programId}/weeks/${weekId}/days/${dayId}`)
        ).data as unknown as Program
    }

    public async updateDayCoachNote(programId: string, weekId: string, dayId: string, notes: string): Promise<Program> {
        return (await this.client.post(`programs/${programId}/weeks/${weekId}/days/${dayId}/notes`, {
            note: notes
        })).data as unknown as Program
    }

    public async updateDayAthleteNote(programId: string, weekId: string, dayId: string, notes: string): Promise<Program> {
        return (await this.client.patch(`programs/${programId}/weeks/${weekId}/days/${dayId}/notes`, {
            note: notes
        })).data as unknown as Program
    }

    public async createExercise(programId: string, weekId: string, dayId: string, name: string, videoRef: string): Promise<Program> {
        return (await this.client.post(`programs/${programId}/weeks/${weekId}/days/${dayId}/exercises`, {
            name: name,
            videoRef: videoRef
        })).data as unknown as Program
    }

    public async createSet(programId: string, weekId: string, dayId: string, exerciseId: string, reps: number, rpe: number, percentage: number, weight: number, percentageReference: PercentageOptions, videoRequested: boolean): Promise<Program> {
        return (await this.client.post(`programs/${programId}/weeks/${weekId}/days/${dayId}/exercises/${exerciseId}/sets`, {
            reps: reps,
            rpe: rpe,
            percentage: percentage,
            weight: weight,
            percentageReference: percentageReference,
            videoRequested: videoRequested
        })).data as unknown as Program
    }

    public async updateExercise(programId: string, weekId: string, dayId: string, exerciseId: string, exercise: Exercise): Promise<Program> {
        return (await this.client.put(`programs/${programId}/weeks/${weekId}/days/${dayId}/exercises/${exerciseId}`, exercise)).data as unknown as Program
    }

    public async deleteExercise(programId: string, weekId: string, dayId: string, exerciseId: string): Promise<Program> {
        return (await this.client.delete(`programs/${programId}/weeks/${weekId}/days/${dayId}/exercises/${exerciseId}`)).data as unknown as Program
    }

    public async finishSet(programId: string, weekId: string, dayId: string, exerciseId: string, setId: string, repsCompleted: number): Promise<Program> {
        return (await this.client.patch(`programs/${programId}/weeks/${weekId}/days/${dayId}/exercises/${exerciseId}/sets/${setId}`, {
            repsDone: repsCompleted
        })).data as unknown as Program
    }

    public async createProgram(name: string, description: string): Promise<Program> {
        return (await this.client.post(`/programs`, {
            name: name,
            description: description
        })).data as unknown as Program
    }

    public async updateProgram(programId: string, name: string, description: string): Promise<Program> {
        return (await this.client.post(`/programs/${programId}`, {
            name: name,
            description: description
        })).data as unknown as Program
    }
}

export default new Api()

