import { Component, Input } from '@angular/core';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { glyphMarkup } from '../utils/vehicle-glyphs';

@Component({
  selector: 'app-vehicle-glyph',
  standalone: true,
  template: `<div class="{{ className }}" [innerHTML]="markup()"></div>`,
})
export class VehicleGlyphComponent {
  @Input() category: string | null | undefined = null;
  @Input() className = 'w-16 h-16';

  constructor(private sanitizer: DomSanitizer) {}

  markup(): SafeHtml {
    return this.sanitizer.bypassSecurityTrustHtml(glyphMarkup(this.category));
  }
}
