import { Component, EventEmitter, Input, Output, forwardRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';

@Component({
  selector: 'app-input',
  standalone: true,
  imports: [CommonModule],
  template: `
    <label class="block">
      @if (label) {
        <span class="block text-sm font-medium text-charcoal mb-1">{{ label }}</span>
      }
      <input
        [type]="type"
        [placeholder]="placeholder"
        [required]="required"
        [disabled]="disabled"
        [attr.min]="min"
        [attr.max]="max"
        [attr.step]="step"
        [attr.maxlength]="maxLength"
        [attr.inputmode]="inputMode"
        [value]="value"
        (input)="onInput($event)"
        (blur)="onTouched()"
        class="w-full border border-mist rounded-sign px-3 py-2.5 bg-white text-ink placeholder:text-charcoal/40 focus:border-ink transition-colors {{ className }}"
      />
      @if (error) {
        <span class="block text-sm text-brick mt-1">{{ error }}</span>
      }
    </label>
  `,
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => InputComponent),
      multi: true,
    },
  ],
})
export class InputComponent implements ControlValueAccessor {
  @Input() label = '';
  @Input() type = 'text';
  @Input() placeholder = '';
  @Input() error = '';
  @Input() className = '';
  @Input() required = false;
  @Input() disabled = false;
  @Input() min?: string | number;
  @Input() max?: string | number;
  @Input() step?: string | number;
  @Input() maxLength?: number;
  @Input() inputMode?: string;
  @Output() valueChange = new EventEmitter<string>();
  @Output() blurred = new EventEmitter<void>();

  value = '';
  private onChange: (value: string) => void = () => {};
  onTouched: () => void = () => {};

  writeValue(value: string): void {
    this.value = value ?? '';
  }
  registerOnChange(fn: (value: string) => void): void {
    this.onChange = fn;
  }
  registerOnTouched(fn: () => void): void {
    this.onTouched = () => {
      fn();
      this.blurred.emit();
    };
  }
  setDisabledState(isDisabled: boolean): void {
    this.disabled = isDisabled;
  }

  onInput(event: Event) {
    const value = (event.target as HTMLInputElement).value;
    this.value = value;
    this.onChange(value);
    this.valueChange.emit(value);
  }
}
