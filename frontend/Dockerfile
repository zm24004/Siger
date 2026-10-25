# -----------------------------------------------------------------------------
# SIGER - Frontend Dockerfile (multi-stage)
# Build: Node 20 + Vite -> Runtime: Nginx Alpine
# -----------------------------------------------------------------------------

# Etapa 1: Build
FROM node:20-alpine AS build
WORKDIR /app

# Args para Vite
ARG VITE_API_BASE_URL=/api
ARG VITE_APP_NAME=SIGER

ENV VITE_API_BASE_URL=$VITE_API_BASE_URL
ENV VITE_APP_NAME=$VITE_APP_NAME

# Dependencias
COPY package*.json ./
RUN npm ci

# Código
COPY . .
RUN npm run build

# Etapa 2: Serve
FROM nginx:1.27-alpine
WORKDIR /usr/share/nginx/html

# Limpiar default
RUN rm -rf /usr/share/nginx/html/*

# Copiar build
COPY --from=build /app/dist .

# Copiar nginx.conf
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
