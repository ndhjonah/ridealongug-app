import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-card',
  standalone: true,
  template: `
    <div class="bg-white border border-mist rounded-sign {{ className }}">
      <ng-content></ng-content>
    </div>
  `,
})
export class CardComponent {
  @Input() className = '';
}
