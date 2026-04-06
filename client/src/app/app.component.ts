import {AfterViewInit, Component, ViewChild} from '@angular/core';
import {ToastComponent} from "./shared/components/ui/toast/toast.component";
import {ToastService} from "./core/services/toast/toast.service";
import {AuthService} from "./core/services/auth/auth.service";

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.css'],
    standalone: false
})
export class AppComponent implements AfterViewInit{

    @ViewChild(ToastComponent, { static: false }) toastRef!: ToastComponent;

    constructor(private toastService: ToastService, private authService: AuthService) {}

    ngAfterViewInit(): void {
        this.toastService.register(this.toastRef);
    }
}
