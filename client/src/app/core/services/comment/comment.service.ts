import {Injectable} from '@angular/core';
import { HttpClient } from "@angular/common/http";
import {Observable, of} from "rxjs";
import {Comment} from "../../models/comment.model";
import {ToastService} from "../toast/toast.service";
import {catchError, tap} from "rxjs/operators";
import {ToastType} from "../../enums/toast-type.enum";

@Injectable({
  providedIn: 'root'
})
export class CommentService {
  private apiUrl = 'http://localhost:8080/customer-1.0/api/comments';

  constructor(private readonly _http: HttpClient,
              private readonly _toastService: ToastService) {}

  getCommentsByCaseId(caseId: number): Observable<Comment[]> {
    return this._http.get<Comment[]>(`${this.apiUrl}/case/${caseId}`);
  }

  updateComment(commentId: number, comment: Comment): Observable<void> {
    return this._http.put<void>(`${this.apiUrl}/${commentId}`, comment).pipe(
        tap(() => {
          this._toastService.show("Comment edited successfully", ToastType.Success);
        }),
        catchError(() => {
          this._toastService.show("Failed to delete comment.", ToastType.Error);
          return of(void 0);
        })
    );
  }

  deleteComment(commentId: number): Observable<void> {
    return this._http.delete<void>(`${this.apiUrl}/${commentId}`).pipe(
        tap((): void => {
          this._toastService.show("Comment deleted successfully", ToastType.Success);
        }),
        catchError(() => {
          this._toastService.show("Failed to delete comment.", ToastType.Error);
          return of(void 0);
        })
    );  }

  addComment(comment: Comment): Observable<Comment> {
    return this._http.post<Comment>(this.apiUrl, comment).pipe(
        tap(() => {
          this._toastService.show("Comment added successfully", ToastType.Success);
        }),
        catchError(() => {
          this._toastService.show("Failed to add comment.", ToastType.Error);
          return of(void 0);
        })
    );
  }
}
