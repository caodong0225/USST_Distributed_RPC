const ENVIRONMENT = process.env.NODE_ENV || 'development';

const config = {
    development: {
        API_URL: 'http://localhost:8082/',
    },
    production: {
        API_URL: '/api/',
    },
}

export default config[ENVIRONMENT];
