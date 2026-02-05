import {Component} from '@angular/core';

@Component({
    selector: 'app-toast',
    templateUrl: './toast.component.html',
    styleUrls: ['./toast.component.scss']
})
export class ToastComponent {
    toasts: { text: string, type: 'success' | 'error' | 'info' }[] = [];

    showToast(text: string, type: 'success' | 'error' | 'info' = 'info') {
        const toast = {text, type};
        this.toasts.push(toast);
        setTimeout(() => this.toasts.shift(), 10000);
    }
}
