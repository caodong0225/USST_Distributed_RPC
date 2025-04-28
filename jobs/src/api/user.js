import { get, post, put, del, request } from "./request";

export async function login(username,password) {
  return post(
    "user/login",
    {
      username: username,
      hash: password,
    },
    false
  );
}

export async function register(username,password,email) {
  return post(
    "user/register",
    {
      username: username,
      hash: password,
      email: email
    },
    false
  );
}

export async function getUserById(id) {
  const user = await get(`user/${id}`);
  return user?.data;
}