import {APP_INITIALIZER, LOCALE_ID, NgModule} from '@angular/core';
import {CommonModule, registerLocaleData} from '@angular/common';
import {I18NEXT_SERVICE, I18NextModule, ITranslationService} from "angular-i18next";
import Backend from "i18next-http-backend";
import localeLt from '@angular/common/locales/lt';


registerLocaleData(localeLt);

export function appInit(i18next: ITranslationService) {
  return () => i18next
      .use(Backend)
      .init({
        whitelist: ['en', 'lt'],
        fallbackLng: 'en',
        lng: 'en',
        returnEmptyString: false,
        ns: ['translation', 'validation'],
        defaultNS: 'translation',
        backend: {
          loadPath: './assets/locales/{{lng}}/{{ns}}.json'
        }
      });
}

export function localeIdFactory(i18next: ITranslationService) {
  return i18next.language;
}

@NgModule({
  imports: [
    CommonModule,
    I18NextModule.forRoot()
  ],
  exports: [
    I18NextModule
  ],
  providers: [
    {
      provide: APP_INITIALIZER,
      useFactory: appInit,
      deps: [I18NEXT_SERVICE],
      multi: true
    },
    {
      provide: LOCALE_ID,
      deps: [I18NEXT_SERVICE],
      useFactory: localeIdFactory
    }
  ]
})

export class I18nextModule { }
