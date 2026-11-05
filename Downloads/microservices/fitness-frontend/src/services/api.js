import axios from "axios";

const API_URL = "/api";

const api = axios.create({
    baseURL: API_URL
});

api.interceptors.request.use((config) => {
    const userId = localStorage.getItem("userId");
    const token = localStorage.getItem("token");

    if (token) {
        config.headers.Authorization = "Bearer " + token;
    }
    if (userId) {
        config.headers["X-User-ID"] = userId;
    }
    return config;
});

export const getActivities = () => api.get("/activities");
export const getActivity = (activityId) => api.get(`/activities/${activityId}`);
export const addActivities = (activity) => api.post("/activities", activity);
export const deleteActivities = (id) => api.delete(`/recommendations/activity/${id}`);
export const getActivityRecommendation = (activityId) =>
    api.get(`/recommendations/activity/${activityId}`);
export const getUserRecommendations = (userId) =>
    api.get(`/recommendations/user/${userId}`);
