import {Component, forwardRef, Input} from "@angular/core";
import {ControlValueAccessor, NG_VALUE_ACCESSOR} from "@angular/forms";

@Component({
    selector: 'app-custom-text-input',
    templateUrl: './custom-text-input.component.html',
    styleUrls: ['./custom-text-input.component.scss'],
    providers: [
        {
            provide: NG_VALUE_ACCESSOR,
            useExisting: forwardRef(() => CustomTextInputComponent),
            multi: true
        }
    ],
    standalone: false
})
export class CustomTextInputComponent implements ControlValueAccessor {
  @Input() multiline = false;

  value: string = '';
  isDisabled: boolean = false;
  touched: boolean = false;

  onChange: (value: string) => void = () => {};
  onTouched: () => void = () => {};

  writeValue(value: string): void {
    this.value = value || '';
    this.touched = false;
  }

  registerOnChange(fn: (value: string) => void): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    this.isDisabled = isDisabled;
  }

  onInput(event: Event): void {
    const input = event.target as HTMLInputElement | HTMLTextAreaElement;
    this.value = input.value;
    this.onChange(this.value);
  }

  onBlur() {
    this.touched = true;
    this.onTouched();
  }
}
