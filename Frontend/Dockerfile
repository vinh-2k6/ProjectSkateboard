
# Stage 1: Build React + Vite
FROM node:22-alpine AS build

WORKDIR /app

COPY package*.json ./
RUN npm ci

COPY . .

ARG VITE_API_URL=http://localhost:8090
ENV VITE_API_URL=$VITE_API_URL

RUN npm run build

# Stage 2: Serve static files with Nginx
FROM nginx:alpine

COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 80
