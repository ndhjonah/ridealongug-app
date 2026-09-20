import { Component, EventEmitter, Input, Output, forwardRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';

@Component({
  selector: 'app-select',
  standalone: true,
  imports: [CommonModule],
  template: `
    <label class="block">
      @if (label) {
        <span class="block text-sm font-medium text-charcoal mb-1">{{ label }}</span>
      }
      <select
        [required]="required"
        [value]="value"
        (change)="onChange_($event)"
        (blur)="onTouched()"
        class="w-full border border-mist rounded-sign px-3 py-2.5 bg-white text-ink focus:border-ink transition-colors {{ className }}"
      >
        <ng-content></ng-content>
      </select>
      @if (error) {
        <span class="block text-sm text-brick mt-1">{{ error }}</span>
      }
    </label>
  `,
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => SelectComponent),
      multi: true,
    },
  ],
})
export class SelectComponent implements ControlValueAccessor {
  @Input() label = '';
  @Input() error = '';
  @Input() className = '';
  @Input() required = false;
  @Output() valueChange = new EventEmitter<string>();

  value = '';
  private onChangeFn: (value: string) => void = () => {};
  onTouched: () => void = () => {};

  writeValue(value: string): void {
    this.value = value ?? '';
  }
  registerOnChange(fn: (value: string) => void): void {
    this.onChangeFn = fn;
  }
  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }
  setDisabledState(): void {}

  onChange_(event: Event) {
    const value = (event.target as HTMLSelectElement).value;
    this.value = value;
    this.onChangeFn(value);
    this.valueChange.emit(value);
  }
}
