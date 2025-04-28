import { get, post, put, del, request } from "./request";

export async function getJobs() {
    const response = await get("jobs/list");
    return response?.data;
}

export async function addJob() {

}