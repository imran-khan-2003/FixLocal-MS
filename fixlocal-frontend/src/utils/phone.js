export function formatPhoneForDisplay(rawPhone) {
  if (!rawPhone) return "";

  const compact = String(rawPhone).trim().replace(/[\s()-]/g, "");
  if (!compact.startsWith("+")) return String(rawPhone).trim();

  const digits = compact.slice(1).replace(/\D/g, "");
  if (!digits) return String(rawPhone).trim();

  // Common case in this app (India): +91XXXXXXXXXX
  if (digits.startsWith("91") && digits.length > 10) {
    return `+91 ${digits.slice(2)}`;
  }

  // Generic fallback: assume last 10 digits are subscriber number.
  if (digits.length > 10) {
    const countryCodeLength = Math.min(3, Math.max(1, digits.length - 10));
    return `+${digits.slice(0, countryCodeLength)} ${digits.slice(countryCodeLength)}`;
  }

  return `+${digits}`;
}
