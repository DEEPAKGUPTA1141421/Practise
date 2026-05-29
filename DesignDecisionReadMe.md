# CarFinder AI — README

## What did I build and why?
A chat-based car recommendation tool. You tell it what you want in plain English, it asks a couple of follow-up questions, then picks the best 3 cars from a dataset of 56 Indian cars and explains why each one fits.

The problem I solved: too many cars, too many specs, no easy way to compare. Most people just want someone to say "buy this one, here's why."

## What did I deliberately cut?
- User accounts / saved searches
- Car comparison side-by-side view
- Filters UI (sliders, dropdowns) — the chat is the filter
- Images of cars
- Price negotiation / dealer links

## Tech stack and why
- **Spring Boot (Java)** — backend was already scaffolded, kept it
- **React + Vite** — fast to set up, simple component model
- **Groq (Llama 3.3 70B)** — free, fast, OpenAI-compatible format so easy to swap
- **In-memory sessions** — no database needed for a demo, keeps it simple
- **Plain CSS Modules** — no Tailwind, just black and white, didn't need a design system

## What I delegated to AI vs did manually

**AI did:**
- All boilerplate (DTOs, Spring config, CORS, component scaffolding)
- First drafts of every service class
- The regex fallback for intent extraction
- CSS layout and chat bubble styles

**I did manually:**
- Designed the 6-step conversation flow (extract → missing fields → follow-up → filter → LLM rank)
- Wrote the body-type map for all 56 cars (no field in the dataset)
- Debugged the duplicate recommendation bug (LLM was repeating the same car when only 1 result matched)
- Tuned the Groq prompts to return clean JSON without markdown fences
- Switched from OpenAI → Gemini → Groq when API keys had quota issues

## Where AI helped most
Scaffolding. What would've taken 2 hours of boilerplate (DTOs, config, CORS, component files) took 10 minutes. Also good at first-draft CSS — I just described the look and it generated something close.

## Where AI got in the way
Prompt had to be very specific. Early versions of the recommendation prompt returned the same car 3 times when the filtered list had only 1 result. Also generated too many abstractions upfront — had to trim things down.

## If I had 4 more hours
- Side-by-side car comparison
- Show car images
- "Why not this car?" — let the user ask about a specific car they had in mind
- Deploy to Railway so there's a live URL
