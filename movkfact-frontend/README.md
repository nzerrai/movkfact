# Movkfact Frontend

React 18.2.0 + Material-UI 5.14.0 + React Router v6 frontend application for the Movkfact data generation platform.

## 🚀 Quick Start

### Prerequisites
- Node.js 16+ 
- npm 8+

### Installation

```bash
cd movkfact-frontend
npm install
```

### Development

```bash
npm start
```

Runs on [http://localhost:3000](http://localhost:3000)

### Build

```bash
npm run build
```

Creates optimized production build in `build/` folder.

## 📁 Folder Structure

```
src/
├── layout/          # Layout components (Header, Sidebar, Layout)
├── pages/           # Page components (Dashboard, DomainsPage, NotFound)
├── components/      # Reusable components (StatCard, etc.)
├── services/        # API client (axios configuration)
├── hooks/           # Custom React hooks (useDomains)
├── utils/           # Utility functions (formatters)
├── theme/           # MUI theme configuration
├── App.jsx          # Main app with routing
└── index.jsx        # React DOM render
```

## 🎨 Theme

Material-UI theme configured with:
- Primary: #1976d2 (Blue)
- Secondary: #4caf50 (Green)
- Roboto font family

## 🛣️ Routes

- `/` - Dashboard home page
- `/domains` - Domain management page
- `/datasets` - Dataset management (S1.5+)
- `/*` - 404 Not Found

## 🔗 API Integration

Backend API configured to `http://localhost:8080` (configurable via `.env.local`)

Update `REACT_APP_API_URL` to point to different API endpoints.

## 🧪 Testing

```bash
npm test
```

## 📱 Responsive Design

- Mobile (<600px): Sidebar hidden, hamburger menu
- Tablet (600px+): Sidebar collapsible
- Desktop (960px+): Full layout with permanent sidebar

## 🔐 Security

- No sensitive data in localStorage (tokens added in S1.7)
- HTTPS enforced in production
- CORS configured on backend

## 📝 Development Notes

### S1.5 Tasks
1. Add Context API for domain state management
2. Implement error handling hooks
3. Connect to backend API for domain list
4. Add domain CRUD forms

### S1.7 Tasks
1. Add user authentication
2. JWT token refresh mechanism
3. Protected routes

## 🤝 Contributing

Follow component naming conventions: PascalCase for components, camelCase for functions.

---

**Status:** Initial project setup (S1.4)  
**Last Updated:** 27 février 2026
