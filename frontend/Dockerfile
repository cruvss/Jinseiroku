FROM node:24-alpine
WORKDIR /app

RUN npm install -g @angular/cli

COPY package*.json ./
RUN npm ci

COPY . .

EXPOSE 4200

CMD ["ng", "serve", "--host", "0.0.0.0", "--poll", "2000"]