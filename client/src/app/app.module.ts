import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';

import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './app.component';
import {SharedModule} from "./shared/shared.module";
import { provideHttpClient, withInterceptorsFromDi } from "@angular/common/http";
import {CoreModule} from "./core/core.module";

@NgModule({ declarations: [
        AppComponent,
    ],
    exports: [],
    bootstrap: [AppComponent], imports: [BrowserModule,
        AppRoutingModule,
        SharedModule,
        CoreModule], providers: [provideHttpClient(withInterceptorsFromDi())] })
export class AppModule {
}
