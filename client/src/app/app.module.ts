import { BrowserModule } from '@angular/platform-browser';
import { NgModule, provideAppInitializer, inject } from '@angular/core';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { SharedModule } from "./shared/shared.module";
import { provideHttpClient, withInterceptorsFromDi } from "@angular/common/http";
import { CoreModule } from "./core/core.module";

import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBarModule } from '@angular/material/snack-bar';

import { AuthService } from "./core/services/auth/auth.service";

@NgModule({
    declarations: [
        AppComponent,
    ],
    imports: [
        BrowserModule,
        AppRoutingModule,
        SharedModule,
        CoreModule,
        MatCardModule,
        MatFormFieldModule,
        MatInputModule,
        MatButtonModule,
        MatProgressSpinnerModule,
        MatSnackBarModule
    ],
    providers: [
        provideHttpClient(withInterceptorsFromDi()),

        provideAppInitializer(() => {
            const authService = inject(AuthService);
            return authService.loadUser().toPromise();
        })
    ],
    bootstrap: [AppComponent]
})
export class AppModule {}