import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {RouterModule} from "@angular/router";
import { CustomTextInputComponent } from './form-controls/custom-text-input/custom-text-input.component';
import { ToastComponent } from './components/ui/toast/toast.component';
import { CapitalizePipe } from './pipes/capitalize.pipe';
import { CamelCaseToTitlePipe } from './pipes/camel-case-to-title.pipe';
import { RemoveUnderscoresPipe } from './pipes/remove-underscores.pipe';

@NgModule({
    declarations: [
        CustomTextInputComponent,
        ToastComponent,
        CapitalizePipe,
        CamelCaseToTitlePipe,
        RemoveUnderscoresPipe,
        ],
    exports: [
        CustomTextInputComponent,
        ToastComponent,
        CapitalizePipe,
        CamelCaseToTitlePipe,
        RemoveUnderscoresPipe,
    ],
    imports: [
        CommonModule,
        RouterModule,
    ]
})
export class SharedModule {
}
