export interface CategoryColor {
  bar: string;
  text: string;
}

export const CATEGORY_COLORS: Record<string, CategoryColor> = {
  Sedan: { bar: '#F2B705', text: '#8a6600' },
  SUV: { bar: '#2F6D4F', text: '#2F6D4F' },
  Pickup: { bar: '#3A3D42', text: '#3A3D42' },
  Tractor: { bar: '#2F6D4F', text: '#2F6D4F' },
  'Sugarcane Truck': { bar: '#B33F2E', text: '#B33F2E' },
};

export function categoryColor(name: string | null | undefined): CategoryColor {
  return (name && CATEGORY_COLORS[name]) || { bar: '#3A3D42', text: '#3A3D42' };
}

export function glyphMarkup(category: string | null | undefined): string {
  const c = categoryColor(category).bar;

  if (category === 'Tractor') {
    return `<svg viewBox="0 0 64 64" fill="none" class="w-full h-full">
      <rect x="8" y="26" width="18" height="12" rx="1" fill="${c}" />
      <rect x="24" y="18" width="12" height="8" rx="1" fill="${c}" />
      <circle cx="16" cy="46" r="8" fill="none" stroke="${c}" stroke-width="3" />
      <circle cx="44" cy="46" r="12" fill="none" stroke="${c}" stroke-width="3" />
      <line x1="36" y1="22" x2="52" y2="34" stroke="${c}" stroke-width="3" />
    </svg>`;
  }
  if (category === 'Sugarcane Truck') {
    return `<svg viewBox="0 0 64 64" fill="none" class="w-full h-full">
      <rect x="4" y="22" width="34" height="16" rx="1" fill="${c}" />
      <path d="M38 26h10l8 8v4H38z" fill="${c}" />
      <circle cx="14" cy="42" r="6" fill="none" stroke="${c}" stroke-width="3" />
      <circle cx="48" cy="42" r="6" fill="none" stroke="${c}" stroke-width="3" />
      <line x1="6" y1="18" x2="34" y2="18" stroke="${c}" stroke-width="2" />
      <line x1="6" y1="14" x2="34" y2="14" stroke="${c}" stroke-width="2" opacity="0.6" />
    </svg>`;
  }
  if (category === 'Pickup') {
    return `<svg viewBox="0 0 64 64" fill="none" class="w-full h-full">
      <path d="M6 32h20V22h8l8 10h16v6H6z" fill="${c}" />
      <circle cx="16" cy="42" r="6" fill="none" stroke="${c}" stroke-width="3" />
      <circle cx="46" cy="42" r="6" fill="none" stroke="${c}" stroke-width="3" />
    </svg>`;
  }
  if (category === 'SUV') {
    return `<svg viewBox="0 0 64 64" fill="none" class="w-full h-full">
      <path d="M8 34c0-8 4-12 10-12h6l6-8h10l8 8h4c4 0 6 4 6 8v6H8z" fill="${c}" />
      <circle cx="18" cy="42" r="6" fill="none" stroke="${c}" stroke-width="3" />
      <circle cx="46" cy="42" r="6" fill="none" stroke="${c}" stroke-width="3" />
    </svg>`;
  }

  return `<svg viewBox="0 0 64 64" fill="none" class="w-full h-full">
    <path d="M8 36c0-3 2-6 6-6l4-8c1-2 3-3 5-3h18c2 0 4 1 5 3l4 8c4 0 6 3 6 6v4H8z" fill="${c}" />
    <circle cx="18" cy="42" r="6" fill="none" stroke="${c}" stroke-width="3" />
    <circle cx="46" cy="42" r="6" fill="none" stroke="${c}" stroke-width="3" />
  </svg>`;
}
