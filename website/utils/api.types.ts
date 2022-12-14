export enum UserType {
    COACH = "Coach",
    ATHLETE = "Athlete"
}

export interface User {
    id: string
    name: string
    email: string
    userType: UserType
    passwordSalt: null // will never be defined in a client response
    passwordHash: null // will never be defined in a client response
}

export interface Athlete extends User {
    weightClass: string
    weight: number
    dob: string
    squatMax: number
    benchMax: number
    deadliftMax: number
    height: number
}

export interface JWTTokenResponse {
    accessToken: string
    refreshToken: string
}

export interface Program {
    id: string
    name: string
    description: string
    weeks: Week[]
    athleteEmail: string
    coachEmail: string
}

export interface Week {
    id: string
    coachNotes: string
    athleteNotes: string
    days: Day[]
}

export interface Day {
    id: string
    coachNotes: string
    athleteNotes: string
    exercises: Exercise[]
}

export interface Exercise {
    id: string
    name: string
    videoRef: string
    sets: Set[]
}

export enum PercentageOptions {
    Squat = "Squat",
    Bench = "Bench",
    Deadlift = "Deadlift"
}

export interface Set {
    id: string
    reps: number
    completedReps: number
    percentage: number
    percentageReference: PercentageOptions,
    weight: number,
    rpe: number,
    videoRef: string,
    videoRequested: boolean
}

export interface ErrorResponse {
    message: string
}

export interface VideoLinkResponse {
    url: string
}

