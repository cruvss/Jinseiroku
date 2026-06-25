import { HttpClient } from "@angular/common/http";
import { inject, Injectable, signal } from "@angular/core";
import { environment } from "../../../environments/environment";
import { InboxItem } from "../models/inbox.model";


@Injectable({ providedIn:'root'})
export class InboxService {
    private http = inject(HttpClient);
    private url = `${environment.apiUrl}/inbox`;
    public unreadCount = signal(0);

    capture(data: FormData){
        return this.http.post(this.url,data);
    }

    list(){
        return this.http.get<{data : InboxItem[]}>(this.url);
    }

    download(id:string){
        return this.http.get(`${this.url}/${id}/download`, {responseType: 'arraybuffer'});
    }

    delete(id:string){
        return this.http.delete(`${this.url}/${id}`);
    }

    markStaged(id:string, type:string, targetId:string){
        return this.http.put(`${this.url}/${id}/staging?type=${type}&targetId=${targetId}`,{});
    }

}