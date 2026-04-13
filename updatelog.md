# Update Log

## 2026-04-13

### UI Modernization (Tailwind + latest HTML/CSS)
Updated the frontend to a cleaner, more user-friendly and professional design using Tailwind CSS.

#### Updated files
- `/home/runner/work/smartparking-susu/smartparking-susu/Smartparking-susu/WebContent/index.html`
- `/home/runner/work/smartparking-susu/smartparking-susu/Smartparking-susu/WebContent/Sign_in_customer.html`
- `/home/runner/work/smartparking-susu/smartparking-susu/Smartparking-susu/WebContent/Sign_up_customer.html`
- `/home/runner/work/smartparking-susu/smartparking-susu/Smartparking-susu/WebContent/link.html`

#### What was changed
- Replaced legacy page structures with modern responsive Tailwind-based layouts.
- Applied a professional dark/slate-indigo color scheme across pages.
- Improved typography, spacing, call-to-actions, and card-based sections for better usability.
- Simplified and refreshed navigation and visual consistency between pages.

#### Non-breaking compatibility safeguards
- Preserved sign-in backend contract:
  - `action="Sign_in_Customer"`
  - input names: `uname`, `pwd`
- Preserved sign-up backend contract:
  - `action="Sign_up_cust_servlet"`
  - input names: `cust_name`, `V_number`, `V_type`, `cust_pwd`
- Kept key page routes and anchor links functional (`Sign_in_customer.html`, `Sign_up_customer.html`, `#gallery`, `#contact`).

### Follow-up Fix
- Corrected homepage email link in `index.html`:
  - from `mailto:example.com`
  - to `mailto:contact@thatsmyspot.com`
