# Haimuoi3 Frontend

E-commerce frontend application built with Angular 20 (LTS), featuring a modern, premium design.

## Features

- 🎨 Modern, premium UI design with Tailwind CSS
- 📱 Fully responsive design
- 🚀 Angular 20 LTS with standalone components
- ⚡ Signal-based state management
- 🎯 Component-based architecture
- 🔄 Auto-sliding hero carousel
- 🛍️ Product catalog with filtering
- 📦 Reusable component library

## Tech Stack

- **Framework**: Angular 20.0 (LTS)
- **Styling**: Tailwind CSS 3.4
- **Language**: TypeScript 5.7
- **State Management**: Angular Signals
- **HTTP Client**: Angular HttpClient
- **Routing**: Angular Router
- **SSR**: Angular Universal

## Project Structure

```
src/
├── app/
│   ├── core/
│   │   └── services/          # Shared services
│   ├── features/
│   │   └── home/              # Home page module
│   │       ├── hero-section/
│   │       ├── category-section/
│   │       ├── new-arrivals/
│   │       ├── policies-section/
│   │       └── testimonials/
│   ├── shared/
│   │   ├── components/        # Reusable components
│   │   ├── interfaces/        # TypeScript interfaces
│   │   └── layout/            # Layout components
│   └── app.routes.ts          # Route configuration
├── styles.scss                # Global styles
└── index.html                 # Main HTML file
```

## Installation

### Prerequisites

- Node.js >= 18.x
- npm >= 11.x

### Steps

1. Clone the repository:
```bash
git clone <repository-url>
cd haimuoi3-frontend
```

2. Install dependencies:
```bash
npm install
```

3. Start the development server:
```bash
npm start
```

4. Open your browser and navigate to:
```
http://localhost:4200
```

## Available Scripts

- `npm start` - Start development server
- `npm run build` - Build for production
- `npm test` - Run unit tests
- `npm run watch` - Build in watch mode

## Components Overview

### Layout Components

- **Header**: Navigation bar with search, cart, and user actions
- **Footer**: Site footer with links and social media

### Page Components

- **Home**: Main landing page with multiple sections
  - **Hero Section**: Full-screen carousel with CTA
  - **Category Section**: Product category showcase
  - **New Arrivals**: Latest products grid
  - **Policies Section**: Company policies and benefits
  - **Testimonials**: Customer reviews

### Shared Components

- **Product Card**: Reusable product display card
- **Category Card**: Category display with hover effects
- **Policy Card**: Policy/benefit information card
- **Testimonial Card**: Customer testimonial card

## Services

- **HeroService**: Manages hero slider data
- **ProductService**: Handles product operations
- **CategoryService**: Manages product categories
- **PolicyService**: Handles company policies
- **TestimonialService**: Manages customer testimonials

## Styling

### Tailwind Configuration

Custom colors defined in `tailwind.config.js`:
- `titanium-graphite`: #1a1a1a
- `titanium-light`: #f4f4f4
- `titanium-accent`: #e5e7eb

### Global Styles

- Glass morphism effects
- Smooth scroll behavior
- Custom transitions and animations

## Development Guidelines

### Component Creation

All components should:
- Be standalone
- Use OnPush change detection
- Use signals for reactive state
- Follow Angular style guide
- Use kebab-case for file names

### Code Style

- Use single quotes for strings
- 2 spaces indentation
- No trailing whitespace
- Use const for immutable variables

## Browser Support

- Chrome (latest)
- Firefox (latest)
- Safari (latest)
- Edge (latest)

## Contributing

1. Fork the repository
2. Create your feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is private and proprietary.

## Contact

For any questions or support, please contact the development team.
