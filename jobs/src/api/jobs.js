import { get, post, put, del, request } from "./request";

export async function getJobs(params, sort, filter) {
    params.size = params.pageSize;

  delete params.pageSize;

  const urlParams = new URLSearchParams(params);
    const response = await get(`jobs/list?${urlParams.toString()}`);
    response.success = true;
    response.list = response?.data?.list;
    response.total = response?.data?.total;
    return response;
}

export async function addJob(data) {
    const response = await post("jobs/add"
        , data
    )
    return response?.data;
}