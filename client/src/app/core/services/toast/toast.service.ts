import {Injectable} from '@angular/core';
import {ToastComponent} from "../../../shared/components/ui/toast/toast.component";
import {ToastType} from "../../enums/toast-type.enum";

@Injectable({
  providedIn: 'root'
})
export class ToastService {
  private toastComponent!: ToastComponent;

  register(toast: ToastComponent): void {
    this.toastComponent = toast;
  }

  show(text: string, type: ToastType = ToastType.Info): void
  {
    this.toastComponent.showToast(text, type);
  }
}
